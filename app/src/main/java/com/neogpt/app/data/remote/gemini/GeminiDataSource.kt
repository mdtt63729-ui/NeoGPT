package com.neogpt.app.data.remote.gemini

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class GeminiDataSource(
    private val okHttpClient: OkHttpClient,
    private val apiKeyProvider: () -> String,
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)
    private val modelListAdapter = moshi.adapter(GeminiModelListResponse::class.java)
    private val fileUploadAdapter = moshi.adapter(FileUploadResponse::class.java)

    fun streamGenerateContent(
        model: String,
        request: GeminiRequest,
    ): Flow<String> = flow {
        val apiKey = apiKeyProvider()
        val url = "$baseUrl/v1beta/models/$model:streamGenerateContent?key=$apiKey&alt=sse"
        val jsonBody = requestAdapter.toJson(request)
        val httpRequest = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API error: ${response.code}")
        }

        val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
        var line: String?
        val stringBuilder = StringBuilder()

        while (reader.readLine().also { line = it } != null) {
            val chunk = line!!.trim()
            if (chunk.startsWith("data:")) {
                val json = chunk.removePrefix("data:").trim()
                if (json.isNotBlank() && json != "[DONE]") {
                    try {
                        val parsed = responseAdapter.fromJson(json)
                        val text = parsed?.candidates
                            ?.firstOrNull()
                            ?.content
                            ?.parts
                            ?.firstOrNull()
                            ?.text
                        if (text != null) {
                            emit(text)
                            stringBuilder.append(text)
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunks
                    }
                }
            }
        }
        reader.close()
        response.close()
    }

    suspend fun listModels(): List<GeminiModelSummary> {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) throw IllegalStateException("Gemini API key is not configured.")
        val url = "$baseUrl/v1beta/models?key=$apiKey"
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()
        response.use {
            if (!it.isSuccessful) {
                throw Exception("Gemini API error: ${it.code}")
            }
            val body = it.body?.string().orEmpty()
            return modelListAdapter.fromJson(body)?.models.orEmpty()
        }
    }

    suspend fun uploadFile(contentResolver: ContentResolver, uri: Uri, displayName: String, mimeType: String): GeminiFile {
        val apiKey = apiKeyProvider()
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read attachment.")
        val startUrl = "$baseUrl/upload/v1beta/files?key=$apiKey"
        val startBody = "{\"file\":{\"display_name\":${moshi.adapter(String::class.java).toJson(displayName)}}}"
        val startRequest = Request.Builder()
            .url(startUrl)
            .header("X-Goog-Upload-Protocol", "resumable")
            .header("X-Goog-Upload-Command", "start")
            .header("X-Goog-Upload-Header-Content-Length", bytes.size.toString())
            .header("X-Goog-Upload-Header-Content-Type", mimeType)
            .post(startBody.toRequestBody("application/json".toMediaType()))
            .build()
        val startResponse = okHttpClient.newCall(startRequest).execute()
        val uploadUrl = startResponse.use {
            if (!it.isSuccessful) error("Gemini file upload could not start: ${it.code}")
            it.header("X-Goog-Upload-URL") ?: it.header("x-goog-upload-url") ?: error("Gemini did not return an upload URL.")
        }
        val uploadRequest = Request.Builder()
            .url(uploadUrl)
            .header("Content-Length", bytes.size.toString())
            .header("X-Goog-Upload-Offset", "0")
            .header("X-Goog-Upload-Command", "upload, finalize")
            .post(bytes.toRequestBody(mimeType.toMediaType()))
            .build()
        val uploadResponse = okHttpClient.newCall(uploadRequest).execute()
        return uploadResponse.use {
            if (!it.isSuccessful) error("Gemini file upload failed: ${it.code}")
            fileUploadAdapter.fromJson(it.body?.string().orEmpty())?.file
                ?: error("Gemini returned an invalid file response.")
        }
    }

    suspend fun generateContent(
        model: String,
        request: GeminiRequest,
    ): GeminiResponse {
        val apiKey = apiKeyProvider()
        val url = "$baseUrl/v1beta/models/$model:generateContent?key=$apiKey"
        val jsonBody = requestAdapter.toJson(request)
        val httpRequest = Request.Builder()
            .url(url)
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(httpRequest).execute()
        if (!response.isSuccessful) {
            throw Exception("Gemini API error: ${response.code}")
        }
        return responseAdapter.fromJson(response.body!!.string())!!
    }
}
