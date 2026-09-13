package com.neogpt.app.data.remote.neo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Built-in, no-user-key backend for the Neo 4.1 Alpha model. */
class NeoAlphaDataSource(private val client: OkHttpClient) {
    companion object {
        const val MODEL_ID = "neo-4.1-alpha"
        const val DISPLAY_NAME = "Neo 4.1 Alpha"
        private const val LLM_ENDPOINT = "https://backend.buildpicoapps.com/aero/run/llm-api?pk=v1-Z0FBQUFBQnFwUWdteHVqR2NfeXBQRk1uaWxiNFB1SWN1YU1ISHBEX1dWSHhyNERpWnMzY2RoOFBFTlFkTl9OTTdDU0ZmclRSTWQ0WEppQjFrMUFWN2VKam1LTG9ESTA3cHc9PQ=="
        private const val IMAGE_ENDPOINT = "https://backend.buildpicoapps.com/aero/run/image-generation-api?pk=v1-Z0FBQUFBQnFwUWdteHVqR2NfeXBQRk1uaWxiNFB1SWN1YU1ISHBEX1dWSHhyNERpWnMzY2RoOFBFTlFkTl9OTTdDU0ZmclRSTWQ0WEppQjFrMUFWN2VKam1LTG9ESTA3cHc9PQ=="
        private const val IMAGE_PERSONA = "Follow instructions precisely! If the user asks to generate, create or make an image, photo, or picture by describing it, You will reply with '/image' + description. Otherwise, You will respond normally. Avoid additional explanations."
    }

    data class ChatResult(val text: String)
    data class ImageResult(val imageUrl: String)

    suspend fun chat(prompt: String, systemPrompt: String = ""): ChatResult = withContext(Dispatchers.IO) {
        val effectivePrompt = buildString {
            if (systemPrompt.isNotBlank()) {
                append("SYSTEM INSTRUCTION — Follow this instruction as the highest-priority user-configured behavior for this conversation. ")
                append(systemPrompt.trim())
                append("\n\n")
            }
            append(IMAGE_PERSONA)
            append("\n\n")
            append(prompt)
        }
        val json = postPrompt(LLM_ENDPOINT, effectivePrompt)
        if (json.optString("status") != "success") {
            error(json.optString("message").ifBlank { "Neo 4.1 Alpha could not respond." })
        }
        ChatResult(json.optString("text"))
    }

    suspend fun generateImage(description: String): ImageResult = withContext(Dispatchers.IO) {
        val json = postPrompt(IMAGE_ENDPOINT, description)
        val imageUrl = json.optString("imageUrl")
        if (json.optString("status") != "success" || imageUrl.isBlank()) {
            error(json.optString("message").ifBlank { "Image generation failed. Please try again." })
        }
        ImageResult(imageUrl)
    }

    private fun postPrompt(endpoint: String, prompt: String): JSONObject {
        val body = JSONObject().put("prompt", prompt).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url(endpoint).post(body).header("Accept", "application/json").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Neo backend returned HTTP ${response.code}.")
            val raw = response.body?.string().orEmpty()
            return runCatching { JSONObject(raw) }.getOrElse { error("Neo backend returned an invalid response.") }
        }
    }
}
