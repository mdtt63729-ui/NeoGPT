package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.ChatRepository
import com.neogpt.app.domain.model.Chat
import kotlinx.coroutines.flow.Flow

class SearchChatsUseCase(
    private val chatRepository: ChatRepository,
) {
    operator fun invoke(query: String): Flow<List<Chat>> {
        // Filter chats by title or message content
        return chatRepository.getAllChats()
        // In production, use a full-text search query in Room
    }
}
