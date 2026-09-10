package com.neogpt.app.data.remote.gemini

import com.neogpt.app.domain.model.Message
import com.neogpt.app.domain.model.Attachment
import com.neogpt.app.domain.model.ModelInfo

object GeminiRequestMapper {
    fun mapMessage(message: Message): Content {
        val parts = mutableListOf<Part>()
        if (message.content.isNotBlank()) {
            parts.add(Part(text = message.content))
        }
        message.attachments.forEach { attachment ->
            when (attachment.type) {
                Attachment.Type.IMAGE -> {
                    parts.add(Part(inlineData = InlineData(
                        mimeType = attachment.mimeType,
                        data = attachment.base64Data ?: "",
                    )))
                }
                Attachment.Type.FILE -> {
                    parts.add(Part(fileData = FileData(
                        mimeType = attachment.mimeType,
                        fileUri = attachment.uri ?: "",
                    )))
                }
                else -> {}
            }
        }
        return Content(
            role = if (message.role == Message.Role.USER) "user" else "model",
            parts = parts,
        )
    }

    fun mapMessages(messages: List<Message>): List<Content> {
        return messages.map { mapMessage(it) }
    }

    fun buildRequest(
        messages: List<Message>,
        model: String,
        temperature: Float? = null,
        maxTokens: Int? = null,
        enableSearch: Boolean = false,
    ): GeminiRequest {
        val contents = mapMessages(messages)
        val tools = mutableListOf<Tool>()
        if (enableSearch) {
            tools.add(Tool(googleSearch = GoogleSearch()))
        }
        return GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = temperature,
                maxOutputTokens = maxTokens,
            ),
            tools = if (tools.isEmpty()) null else tools,
        )
    }
}

object GeminiResponseMapper {
    fun mapResponse(response: GeminiResponse): Message {
        val text = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("") { it.text ?: "" }
            ?: ""
        return Message(
            id = System.currentTimeMillis().toString(),
            role = Message.Role.AI,
            content = text,
            timestamp = System.currentTimeMillis(),
        )
    }

    fun mapChunkToText(chunk: String): String {
        return chunk
    }

    fun mapModelInfo(modelId: String, displayName: String): ModelInfo {
        return ModelInfo(
            id = modelId,
            name = displayName,
            description = "Gemini model",
            speedBadge = "Fast",
            contextWindow = "1M tokens",
            capabilities = listOf("text", "vision", "code"),
        )
    }
}
