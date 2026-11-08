/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 *
 * GhostGram C1 (PLAN.md 11.4): the braille-spoiler conversion, extracted
 * verbatim from the donor's ChatsPasswordHelper.toBrailleSpoiler().
 * In the donor, the ONLY consumer is ChatsPasswordHelper.replaceStringToSpoilers()
 * - the locked-chats spoiler renderer, which is G6/C3-batch territory
 * (biometric / locked chats) and is not ported yet; the donor also keeps
 * that braille branch unreachable (its random gate is hardcoded off).
 * This helper therefore lands the conversion itself plus the key gate now;
 * the C3 batch wires it into the locked-chat rendering points when
 * ChatsPasswordHelper is ported. The key lives in CherrygramMessagesConfig
 * (relocated in PLAN 11.0); nothing remote can flip it.
 */

package uz.unnarsx.cherrygram.chats.helpers

import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig
import kotlin.random.Random

object BrailleSpoilerHelper {

    /** Donor logic, verbatim: non-whitespace chars become random braille cells. */
    fun toBrailleSpoiler(input: String?): String {
        if (input.isNullOrEmpty()) return ""

        val sb = StringBuilder(input.length)
        for (char in input) {
            if (char.isWhitespace()) {
                sb.append(char)
            } else {
                val brailleCode = 0x2801 + Random.nextInt(255)
                sb.append(brailleCode.toChar())
            }
        }
        return sb.toString()
    }

    /**
     * Key-gated form for rendering call sites: converts only when the
     * (local, relocated) useBrailleSpoiler key is on; otherwise returns
     * the text untouched so callers can apply it unconditionally.
     */
    fun replaceWithBrailleSpoilers(originalText: String?): String? {
        if (originalText == null) {
            return null
        }
        return if (CherrygramMessagesConfig.useBrailleSpoiler) {
            toBrailleSpoiler(originalText)
        } else {
            originalText
        }
    }

}
