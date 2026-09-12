package com.neogpt.app.data.remote.gemini

import okhttp3.MultipartBody
import retrofit2.http.*
import retrofit2.http.Streaming
import okhttp3.RequestBody
import okhttp3.ResponseBody

interface GeminiApi {
    @Streaming
    @POST("v1beta/models/{model}:streamGenerateContent")
    suspend fun streamGenerateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest,
    ): ResponseBody

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest,
    ): GeminiResponse

    @Multipart
    @POST("v1beta/files:upload")
    suspend fun uploadFile(
        @Query("key") apiKey: String,
        @retrofit2.http.Part file: MultipartBody.Part,
    ): FileUploadResponse
}
