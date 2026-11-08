package desu.inugram.helpers.translate

import desu.inugram.InuConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import org.json.JSONArray
import org.json.JSONObject

/**
 * GhostGram's built-in translation provider for pluginless builds.
 *
 * inugram's translation-provider slot is normally filled by a plugin; with the
 * plugin engine excluded there is no provider at all and chat translation
 * falls back to Telegram's Premium-gated server path. This engine fills the
 * slot with a direct client-side Google Translate implementation:
 *
 * - Default: the free, keyless `translate.googleapis.com/translate_a/single`
 *   (client=gtx) endpoint. It is an *unofficial* endpoint: no SLA, it may
 *   rate-limit or change without notice.
 * - The user can override the endpoint in Translator settings; an endpoint
 *   pointing at the official Cloud Translation API (`language/translate/v2`)
 *   is called with the official JSON API and the optional user-supplied key.
 *
 * Networking runs on a private single-thread executor; request tokens are
 * tracked so [TranslationProviderHelper.cancelRequest] can drop completions
 * for requests the stock code already cancelled.
 */
object BuiltinTranslateEngine {
    private const val DEFAULT_BASE = "https://translate.googleapis.com/translate_a/single"
    private const val OFFICIAL_HINT = "language/translate/v2"
    private const val MAX_CHUNK = 4000

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "ghostgram-translate").apply { isDaemon = true }
    }
    private val inflight = ConcurrentHashMap<Long, Boolean>()

    fun begin(key: Long) {
        inflight[key] = true
    }

    /** Returns false when the request was cancelled meanwhile. */
    fun finish(key: Long): Boolean = inflight.remove(key) != null

    fun cancel(key: Long): Boolean = inflight.remove(key) != null

    fun translate(
        texts: List<String>,
        fromLangs: List<String?>,
        toLanguage: String,
        done: (List<String>?) -> Unit,
    ) {
        executor.execute {
            done(runCatching { translateBlocking(texts, fromLangs, toLanguage) }.getOrNull())
        }
    }

    private fun translateBlocking(
        texts: List<String>,
        fromLangs: List<String?>,
        toLanguage: String,
    ): List<String>? {
        val to = normalizeLang(toLanguage) ?: "en"
        val endpoint = InuConfig.TRANSLATE_ENDPOINT.value.trim()
        val apiKey = InuConfig.TRANSLATE_API_KEY.value.trim()
        if (endpoint.contains(OFFICIAL_HINT)) {
            return translateOfficial(endpoint, apiKey, texts, to)
        }
        val base = endpoint.ifEmpty { DEFAULT_BASE }
        val out = ArrayList<String>(texts.size)
        for ((index, text) in texts.withIndex()) {
            if (text.isBlank()) {
                out.add(text)
                continue
            }
            val sl = normalizeLang(fromLangs.getOrNull(index)) ?: "auto"
            out.add(translateFree(base, text, sl, to) ?: return null)
        }
        return out
    }

    private fun normalizeLang(lang: String?): String? {
        val value = lang?.trim()?.lowercase().orEmpty()
        return when (value) {
            "", "und", "unknown" -> null
            "zh", "zh-hans", "zh-cn" -> "zh-CN"
            "zh-hant", "zh-tw", "zh-hk" -> "zh-TW"
            else -> value
        }
    }

    private fun translateFree(base: String, text: String, sl: String, tl: String): String? {
        val sb = StringBuilder()
        for (chunk in chunkText(text)) {
            val url = StringBuilder(base)
                .append(if (base.contains('?')) '&' else '?')
                .append("client=gtx&sl=").append(URLEncoder.encode(sl, "UTF-8"))
                .append("&tl=").append(URLEncoder.encode(tl, "UTF-8"))
                .append("&dt=t&q=").append(URLEncoder.encode(chunk, "UTF-8"))
            val body = httpGet(url.toString()) ?: return null
            val root = JSONArray(body)
            val segments = root.optJSONArray(0) ?: return null
            for (i in 0 until segments.length()) {
                val segment = segments.optJSONArray(i) ?: continue
                sb.append(segment.optString(0))
            }
        }
        return sb.toString()
    }

    private fun translateOfficial(
        endpoint: String,
        apiKey: String,
        texts: List<String>,
        to: String,
    ): List<String>? {
        val url = if (apiKey.isEmpty() || endpoint.contains("key=")) endpoint
            else endpoint + (if (endpoint.contains('?')) '&' else '?') + "key=" +
                URLEncoder.encode(apiKey, "UTF-8")
        val payload = JSONObject().apply {
            put("q", JSONArray(texts))
            put("target", to)
            put("format", "text")
        }
        val body = httpPostJson(url, payload.toString()) ?: return null
        val translations = JSONObject(body).optJSONObject("data")?.optJSONArray("translations")
            ?: return null
        if (translations.length() != texts.size) return null
        return List(texts.size) { i -> translations.getJSONObject(i).getString("translatedText") }
    }

    private fun chunkText(text: String): List<String> {
        if (text.length <= MAX_CHUNK) return listOf(text)
        val chunks = ArrayList<String>()
        var rest = text
        while (rest.length > MAX_CHUNK) {
            var cut = rest.lastIndexOf('\n', MAX_CHUNK)
            if (cut < MAX_CHUNK / 2) cut = rest.lastIndexOf(' ', MAX_CHUNK)
            if (cut < MAX_CHUNK / 2) cut = MAX_CHUNK
            chunks.add(rest.substring(0, cut))
            rest = rest.substring(cut)
        }
        if (rest.isNotEmpty()) chunks.add(rest)
        return chunks
    }

    private fun httpGet(url: String): String? = openConnection(url) { connection ->
        connection.requestMethod = "GET"
        connection
    }

    private fun httpPostJson(url: String, json: String): String? = openConnection(url) { connection ->
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { it.write(json) }
        connection
    }

    private inline fun openConnection(url: String, configure: (HttpURLConnection) -> HttpURLConnection): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) GhostGram")
        configure(connection)
        if (connection.responseCode !in 200..299) return null
        return BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { it.readText() }
    }
}
