package com.neogpt.app.ai

import android.net.Uri

enum class AiProvider(val id: String, val displayName: String, val baseUrl: String) {
    NEO_ALPHA("neo", "Neo Built-in", ""),
    GEMINI("gemini", "Google Gemini", "https://generativelanguage.googleapis.com"),
    OPENROUTER("openrouter", "OpenRouter", "https://openrouter.ai/api/v1"),
    NVIDIA("nvidia", "NVIDIA NIM", "https://integrate.api.nvidia.com/v1");

    companion object {
        fun fromId(id: String): AiProvider = entries.firstOrNull { it.id == id } ?: GEMINI
    }
}

data class AiModelRef(val provider: AiProvider, val modelId: String) {
    val encodedId: String get() = "${provider.id}:$modelId"

    companion object {
        fun parse(value: String): AiModelRef {
            val separator = value.indexOf(':')
            if (separator <= 0) return AiModelRef(AiProvider.GEMINI, value)
            return AiModelRef(
                provider = AiProvider.fromId(value.substring(0, separator)),
                modelId = value.substring(separator + 1),
            )
        }
    }
}

fun encodeModel(provider: AiProvider, modelId: String): String = AiModelRef(provider, modelId).encodedId
