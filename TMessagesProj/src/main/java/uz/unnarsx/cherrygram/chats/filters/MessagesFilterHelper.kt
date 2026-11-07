package uz.unnarsx.cherrygram.chats.filters

import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.ChatObject
import org.telegram.messenger.KeywordMessageFilterManager
import org.telegram.messenger.MessageObject
import org.telegram.messenger.MessagesController
import org.telegram.tgnet.TLRPC
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig

/**
 * GhostGram local implementation (PLAN.md 12.0 / 12.4).
 *
 * The donor's public source references this class from ~10 call sites but does
 * not ship it (same incompleteness class as the unpublished Extra.kt), so the
 * API surface below is reconstructed from those call sites and the semantics
 * are driven by the already-ported CherrygramMessagesConfig msgFilters* keys:
 *
 *  - shouldBlockMessage: master switch + chat exclusions + sender-category
 *    exclusions + hide-from-blocked + keyword match. The keyword source is the
 *    UNION of the Cherry element list and the base's own local
 *    KeywordMessageFilterManager (Mango's local keyword filter is preserved as
 *    a subset - one engine, no dual filters; the base manager self-gates on
 *    its own enabled flag, keyword list, whitelist and blocked usernames).
 *  - addSpoilerEntities: when a blocked message is in "hide all under spoiler"
 *    mode, appends a full-length spoiler entity.
 *  - spoilerText: notification text replaced char-by-char with the braille
 *    spoiler glyphs the donor uses for the same purpose.
 *  - Excluded-chats storage: a plain string list in mainconfig. The donor's
 *    exact storage key is not visible in the public source, so a GhostGram key
 *    is used (registered in PLAN.md 12.4).
 *
 * Nothing here touches the network: no remote lists, no remote switches.
 */
object MessagesFilterHelper {

    private const val EXCLUDED_LIST_KEY = "CG_MsgFiltersExcludedChats"

    private val spoilerChars = charArrayOf('⠌', '⡢', '⢑', '⠨', '⠥', '⠮', '⡑')

    // ------------------------------------------------------------------
    // Excluded chats list storage (used by MessageFiltersPreferencesEntry)
    // ------------------------------------------------------------------

    fun getExcludedList(): String = EXCLUDED_LIST_KEY

    fun getArrayList(key: String): ArrayList<String> {
        val prefs = ApplicationLoader.applicationContext
            .getSharedPreferences("mainconfig", android.content.Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, null) ?: return ArrayList()
        return ArrayList(set)
    }

    fun saveArrayList(list: ArrayList<String>, key: String) {
        val prefs = ApplicationLoader.applicationContext
            .getSharedPreferences("mainconfig", android.content.Context.MODE_PRIVATE)
        prefs.edit().putStringSet(key, HashSet(list)).apply()
    }

    fun getExcludedChatsCount(): Int = getArrayList(EXCLUDED_LIST_KEY).size

    private fun excludedDialogIds(): Set<Long> =
        getArrayList(EXCLUDED_LIST_KEY).mapNotNull { it.toLongOrNull() }.toSet()

    // ------------------------------------------------------------------
    // Core predicate
    // ------------------------------------------------------------------

    fun shouldBlockMessage(messageObject: MessageObject?): Boolean {
        if (messageObject == null || messageObject.messageOwner == null) return false
        if (messageObject.isOutOwner || messageObject.isSponsored()) return false

        val config = CherrygramMessagesConfig
        val cherryOn = config.enableMsgFilters
        val account = messageObject.currentAccount

        // The base's local keyword engine self-gates (enabled flag, keywords,
        // whitelist, blocked usernames); a hit there blocks regardless of the
        // Cherry master switch, which is what "base capability preserved as a
        // subset" means in practice.
        val baseHit = KeywordMessageFilterManager.getInstance(account)
            .getMatchingKeyword(messageObject, false) != null
        if (!cherryOn && !baseHit) return false

        if (excludedDialogIds().contains(messageObject.dialogId)) return false

        if (cherryOn) {
            val controller = MessagesController.getInstance(account)
            val fromId = messageObject.messageOwner.from_id
            val senderUserId = (fromId as? TLRPC.TL_peerUser)?.user_id ?: 0L
            val senderUser = if (senderUserId != 0L) controller.getUser(senderUserId) else null
            val dialogId = messageObject.dialogId
            val chat = if (dialogId < 0) controller.getChat(-dialogId) else null

            // Sender-category exclusions: these categories are exempt.
            if (senderUser != null) {
                if (senderUser.bot && config.excludeBots) return false
                if (!senderUser.bot && config.excludeUsers) return false
            }
            if (chat != null) {
                val isChannel = ChatObject.isChannel(chat)
                val isGroup = ChatObject.isMegagroup(chat) || (!isChannel && chat.id > 0)
                if (isChannel && !ChatObject.isMegagroup(chat) && config.excludeChannels) return false
                if (isGroup && config.excludeGroups) return false
            }

            // Hide everything from Telegram-blocked peers.
            if (config.msgFiltersHideFromBlocked && dialogId != 0L &&
                controller.blockePeers.get(dialogId) == 1
            ) {
                return true
            }
        }

        if (baseHit) return true
        if (!cherryOn) return false

        val elements = parseElements(config.msgFiltersElements)
        if (elements.isEmpty()) return false

        val text = buildSearchText(messageObject, config.msgFiltersDetectEntities)
        if (text.isEmpty()) return false

        return matchesAny(text, elements, config.msgFiltersMatchExactWord, config.msgFiltersDetectTranslit)
    }

    private fun parseElements(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split('\n', ',', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun buildSearchText(messageObject: MessageObject, includeEntities: Boolean): String {
        val builder = StringBuilder()
        messageObject.messageText?.let { builder.append(it).append('\n') }
        messageObject.caption?.let { builder.append(it).append('\n') }
        if (builder.isEmpty()) {
            messageObject.messageOwner.message?.let { builder.append(it).append('\n') }
        }
        if (includeEntities) {
            // "Detect in entities": also search the text that entities point at
            // (webpage title/description, poll question) - those strings are not
            // part of the visible message text.
            val media = messageObject.messageOwner.media
            val webpage = (media as? TLRPC.TL_messageMediaWebPage)?.webpage
            if (webpage != null) {
                webpage.title?.let { builder.append(it).append('\n') }
                webpage.description?.let { builder.append(it).append('\n') }
            }
            val poll = (media as? TLRPC.TL_messageMediaPoll)?.poll
            poll?.question?.text?.let { builder.append(it).append('\n') }
        }
        return builder.toString()
    }

    private fun matchesAny(
        text: String,
        elements: List<String>,
        exactWord: Boolean,
        detectTranslit: Boolean
    ): Boolean {
        val haystacks = HashSet<String>()
        haystacks.add(text.lowercase())
        if (detectTranslit) haystacks.add(transliterate(text.lowercase()))
        for (element in elements) {
            val needles = HashSet<String>()
            needles.add(element.lowercase())
            if (detectTranslit) needles.add(transliterate(element.lowercase()))
            for (haystack in haystacks) {
                for (needle in needles) {
                    if (needle.isEmpty()) continue
                    val hit = if (exactWord) {
                        Regex("(?<![\\p{L}\\p{N}_])" + Regex.escape(needle) + "(?![\\p{L}\\p{N}_])")
                            .containsMatchIn(haystack)
                    } else {
                        haystack.contains(needle)
                    }
                    if (hit) return true
                }
            }
        }
        return false
    }

    /**
     * Cyrillic -> Latin transliteration (GOST-style single/multi-char map),
     * used by the donor's "detect transliterated text" option: a message typed
     * in Latin transliteration still matches Cyrillic filter elements and the
     * other way round (both sides are transliterated before matching).
     */
    private fun transliterate(input: String): String {
        val builder = StringBuilder(input.length)
        for (ch in input) {
            builder.append(
                when (ch) {
                    'а' -> "a"; 'б' -> "b"; 'в' -> "v"; 'г' -> "g"; 'д' -> "d"
                    'е' -> "e"; 'ё' -> "e"; 'ж' -> "zh"; 'з' -> "z"; 'и' -> "i"
                    'й' -> "y"; 'к' -> "k"; 'л' -> "l"; 'м' -> "m"; 'н' -> "n"
                    'о' -> "o"; 'п' -> "p"; 'р' -> "r"; 'с' -> "s"; 'т' -> "t"
                    'у' -> "u"; 'ф' -> "f"; 'х' -> "h"; 'ц' -> "ts"; 'ч' -> "ch"
                    'ш' -> "sh"; 'щ' -> "sch"; 'ъ' -> ""; 'ы' -> "y"; 'ь' -> ""
                    'э' -> "e"; 'ю' -> "yu"; 'я' -> "ya"
                    else -> ch.toString()
                }
            )
        }
        return builder.toString()
    }

    // ------------------------------------------------------------------
    // Spoiler presentation
    // ------------------------------------------------------------------

    fun addSpoilerEntities(messageObject: MessageObject?): ArrayList<TLRPC.MessageEntity> =
        addSpoilerEntities(messageObject, messageObject?.messageOwner?.entities, null)

    fun addSpoilerEntities(
        messageObject: MessageObject?,
        entities: ArrayList<TLRPC.MessageEntity>?,
        text: CharSequence?
    ): ArrayList<TLRPC.MessageEntity> {
        val result = ArrayList<TLRPC.MessageEntity>()
        if (entities != null) result.addAll(entities)
        if (messageObject == null || messageObject.messageOwner == null) return result
        if (!CherrygramMessagesConfig.msgFiltersHideAllUnderSpoiler) return result
        if (!shouldBlockMessage(messageObject)) return result
        val length = text?.length
            ?: messageObject.messageText?.length
            ?: messageObject.messageOwner.message?.length
            ?: 0
        if (length <= 0) return result
        val spoiler = TLRPC.TL_messageEntitySpoiler()
        spoiler.offset = 0
        spoiler.length = length
        result.add(spoiler)
        return result
    }

    fun spoilerText(messageObject: MessageObject?, text: CharSequence?): CharSequence? {
        if (text == null) return null
        if (messageObject == null || !shouldBlockMessage(messageObject)) return text
        if (!CherrygramMessagesConfig.msgFiltersHideAllUnderSpoiler) return text
        val builder = StringBuilder(text.length)
        for (i in text.indices) {
            val ch = text[i]
            builder.append(if (ch == ' ' || ch == '\n') ch else spoilerChars[i % spoilerChars.size])
        }
        return builder.toString()
    }
}
