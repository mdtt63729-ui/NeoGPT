package com.neogpt.app.domain.usecase

import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.gemini.GeminiRequest
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

class StreamResponseUseCase(
    private val geminiDataSource: GeminiDataSource,
) {
    operator fun invoke(
        messages: List<Message>,
        modelId: String,
    ): Flow<String> {
        val request = GeminiRequestMapper.buildRequest(messages, modelId)
        return geminiDataSource.streamGenerateContent(modelId, request)
    }
}
