package com.neogpt.app.data.remote.gemini

import com.neogpt.app.domain.model.Attachment
import com.neogpt.app.domain.model.Message
import com.neogpt.app.domain.model.ModelInfo

object GeminiRequestMapper {
    fun mapMessage(message: Message): Content {
        val parts = mutableListOf<Part>()
        if (message.content.isNotBlank()) parts.add(Part(text = message.content))
        message.attachments.forEach { attachment ->
            // Uploaded Gemini files should be referenced through fileData. Inline
            // bytes are used only when no provider file URI exists.
            if (!attachment.uri.isNullOrBlank()) {
                parts.add(Part(fileData = FileData(mimeType = attachment.mimeType, fileUri = attachment.uri)))
            } else if (attachment.type == Attachment.Type.IMAGE && !attachment.base64Data.isNullOrBlank()) {
                parts.add(Part(inlineData = InlineData(mimeType = attachment.mimeType, data = attachment.base64Data)))
            }
        }
        return Content(role = if (message.role == Message.Role.USER) "user" else "model", parts = parts)
    }

    fun mapMessages(messages: List<Message>): List<Content> = messages.map(::mapMessage)

    fun buildRequest(messages: List<Message>, model: String, temperature: Float? = null, maxTokens: Int? = null, enableSearch: Boolean = false, systemPrompt: String? = null): GeminiRequest {
        val contents = mapMessages(messages)
        val tools = mutableListOf<Tool>()
        if (enableSearch) tools.add(Tool(googleSearch = GoogleSearch()))
        return GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(temperature = temperature, maxOutputTokens = maxTokens),
            tools = tools.takeIf { it.isNotEmpty() },
            systemInstruction = systemPrompt?.takeIf { it.isNotBlank() }?.let { Content(role = "user", parts = listOf(Part(text = it))) },
        )
    }
}

object GeminiResponseMapper {
    fun mapResponse(response: GeminiResponse): Message {
        val text = response.candidates?.firstOrNull()?.content?.parts?.joinToString("") { it.text ?: "" } ?: ""
        return Message(System.currentTimeMillis().toString(), Message.Role.AI, text, timestamp = System.currentTimeMillis())
    }
    fun mapChunkToText(chunk: String): String = chunk
    fun mapModelInfo(modelId: String, displayName: String): ModelInfo = ModelInfo(modelId, displayName, "Gemini model", "Fast", "1M tokens", listOf("text", "vision", "code"))
}
