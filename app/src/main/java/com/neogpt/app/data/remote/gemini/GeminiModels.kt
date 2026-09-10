package com.neogpt.app.data.remote.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null,
    val tools: List<Tool>? = null,
)

@JsonClass(generateAdapter = true)
data class Content(
    val role: String = "user",
    val parts: List<Part>,
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    @Json(name = "inline_data") val inlineData: InlineData? = null,
    @Json(name = "file_data") val fileData: FileData? = null,
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String, // base64 encoded
)

@JsonClass(generateAdapter = true)
data class FileData(
    val mimeType: String,
    @Json(name = "file_uri") val fileUri: String,
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    @Json(name = "max_output_tokens") val maxOutputTokens: Int? = null,
)

@JsonClass(generateAdapter = true)
data class SafetySetting(
    val category: String,
    val threshold: String,
)

@JsonClass(generateAdapter = true)
data class Tool(
    @Json(name = "google_search") val googleSearch: GoogleSearch? = null,
    @Json(name = "function_declarations") val functionDeclarations: List<FunctionDeclaration>? = null,
)

@JsonClass(generateAdapter = true)
data class GoogleSearch(val dummy: String = "")

@JsonClass(generateAdapter = true)
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>? = null,
)

// Response models
@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    @Json(name = "usage_metadata") val usageMetadata: UsageMetadata? = null,
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    @Json(name = "finish_reason") val finishReason: String? = null,
    val index: Int? = null,
)

@JsonClass(generateAdapter = true)
data class UsageMetadata(
    @Json(name = "prompt_token_count") val promptTokenCount: Int? = null,
    @Json(name = "candidates_token_count") val candidatesTokenCount: Int? = null,
    @Json(name = "total_token_count") val totalTokenCount: Int? = null,
)

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val file: GeminiFile? = null,
)

@JsonClass(generateAdapter = true)
data class GeminiFile(
    val name: String,
    val displayName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: String? = null,
    val uri: String? = null,
)
