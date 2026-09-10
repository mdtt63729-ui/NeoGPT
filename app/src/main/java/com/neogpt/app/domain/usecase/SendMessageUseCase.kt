package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.ChatRepository
import com.neogpt.app.domain.model.Attachment
import kotlinx.coroutines.flow.Flow

class SendMessageUseCase(
    private val chatRepository: ChatRepository,
) {
    suspend operator fun invoke(
        chatId: String,
        content: String,
        modelId: String,
        attachments: List<Attachment> = emptyList(),
    ): Flow<String> {
        return chatRepository.sendMessage(chatId, content, modelId, attachments)
    }
}
