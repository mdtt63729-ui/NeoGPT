package com.neogpt.app.data.remote.openai

import android.content.ContentResolver
import android.net.Uri
import com.neogpt.app.files.AttachmentContentReader
import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/** OpenAI-compatible transport used by OpenRouter and NVIDIA NIM. */
class OpenAiCompatibleDataSource(
    private val client: OkHttpClient,
    private val apiKeyProvider: () -> String,
    private val baseUrl: String,
    private val providerName: String,
) {
    fun streamChat(
        model: String,
        messages: List<ProviderMessage>,
        systemPrompt: String = "",
    ): Flow<String> = flow {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isBlank()) error("$providerName API key is not configured. Open Settings and add one.")

        val body = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray().also { array ->
                systemPrompt.trim().takeIf { it.isNotBlank() }?.let { array.put(ProviderMessage("system", it).toJson()) }
                messages.forEach { array.put(it.toJson()) }
            })
            put("stream", true)
            put("temperature", 0.7)
        }.toString()

        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .header("X-Title", "Neo GPT")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val detail = response.body?.string().orEmpty().take(500)
                error("$providerName API error ${response.code}: ${detail.ifBlank { response.message }}")
            }
            val source = response.body?.byteStream() ?: error("$providerName returned an empty response.")
            BufferedReader(InputStreamReader(source, Charsets.UTF_8)).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload.isBlank() || payload == "[DONE]") continue
                    val json = runCatching { JSONObject(payload) }.getOrNull() ?: continue
                    val choices = json.optJSONArray("choices") ?: continue
                    val delta = choices.optJSONObject(0)?.optJSONObject("delta") ?: continue
                    val text = delta.optString("content", "")
                    if (text.isNotEmpty()) emit(text)
                }
            }
        }
    }

    suspend fun buildUserMessage(
        contentResolver: ContentResolver,
        text: String,
        attachments: List<ProviderAttachment>,
    ): ProviderMessage {
        val content = JSONArray()
        if (text.isNotBlank()) content.put(JSONObject().put("type", "text").put("text", text))
        attachments.forEach { attachment ->
            val bytes = contentResolver.openInputStream(attachment.uri)?.use { it.readBytes() }
                ?: error("Unable to read ${attachment.name}.")
            if (bytes.size > MAX_ATTACHMENT_BYTES) {
                error("${attachment.name} is larger than the ${MAX_ATTACHMENT_BYTES / 1024 / 1024} MB provider attachment limit.")
            }
            val mime = attachment.mimeType.ifBlank { "application/octet-stream" }
            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
            when {
                mime.startsWith("image/") -> {
                    content.put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:$mime;base64,$encoded")))
                }
                mime == "application/pdf" -> {
                    content.put(
                        JSONObject().put("type", "file").put(
                            "file", JSONObject()
                                .put("filename", attachment.name)
                                .put("file_data", "data:$mime;base64,$encoded")
                        )
                    )
                }
                mime.startsWith("text/") || mime == "application/json" || mime == "application/xml" ||
                    attachment.name.endsWith(".zip", true) || attachment.name.endsWith(".jar", true) || attachment.name.endsWith(".apk", true) ||
                    attachment.name.endsWith(".docx", true) || attachment.name.endsWith(".xlsx", true) || attachment.name.endsWith(".pptx", true) -> {
                    val extracted = AttachmentContentReader.readForPrompt(contentResolver, attachment.uri, attachment.name, mime)
                    content.put(JSONObject().put("type", "text").put("text", "\n\n--- ${attachment.name} ---\n$extracted"))
                }
                else -> error("$providerName chat does not support ${attachment.mimeType} attachments in this build. Images, PDF and text files are supported.")
            }
        }
        return ProviderMessage("user", content)
    }

    data class ProviderAttachment(val uri: Uri, val name: String, val mimeType: String)
    data class ProviderMessage(val role: String, val content: Any) {
        fun toJson(): JSONObject = JSONObject().put("role", role).put("content", content)
    }

    companion object {
        private const val MAX_ATTACHMENT_BYTES = 15L * 1024L * 1024L
    }
}
