package com.neogpt.app.data.remote.gemini

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiStreamParser {
    fun parseStream(rawChunks: Flow<String>): Flow<String> = flow {
        rawChunks.collect { chunk ->
            emit(chunk)
        }
    }

    fun parseJsonStream(jsonLine: String): String? {
        return try {
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(GeminiResponse::class.java)
            val response = adapter.fromJson(jsonLine)
            response?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
        } catch (e: Exception) {
            null
        }
    }
}
