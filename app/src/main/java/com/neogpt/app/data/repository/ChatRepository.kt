package com.neogpt.app.data.repository

import com.neogpt.app.data.local.dao.ChatDao
import com.neogpt.app.data.local.dao.MessageDao
import com.neogpt.app.data.local.entity.ChatEntity
import com.neogpt.app.data.local.entity.MessageEntity
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.data.remote.gemini.GeminiResponseMapper
import com.neogpt.app.domain.model.Chat
import com.neogpt.app.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val geminiDataSource: GeminiDataSource,
) {
    fun getAllChats(): Flow<List<Chat>> =
        chatDao.getAllChats().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun createChat(modelId: String): Chat {
        val chat = Chat(
            id = System.currentTimeMillis().toString(),
            modelId = modelId,
        )
        chatDao.insertChat(chat.toEntity())
        return chat
    }

    fun getMessages(chatId: String): Flow<List<Message>> =
        messageDao.getMessagesForChat(chatId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun sendMessage(
        chatId: String,
        content: String,
        modelId: String,
        attachments: List<com.neogpt.app.domain.model.Attachment> = emptyList(),
    ): Flow<String> {
        // Save user message
        val userMessage = Message(
            id = System.currentTimeMillis().toString(),
            role = Message.Role.USER,
            content = content,
            attachments = attachments,
        )
        messageDao.insertMessage(userMessage.toEntity(chatId))

        // Get chat history
        val history = getMessagesSync(chatId)

        // Build Gemini request
        val request = GeminiRequestMapper.buildRequest(
            messages = history,
            model = modelId,
        )

        // Stream response
        return geminiDataSource.streamGenerateContent(modelId, request)
    }

    private suspend fun getMessagesSync(chatId: String): List<Message> {
        // This would use a suspend query; simplified for architecture
        return emptyList()
    }

    // Mappers
    private fun ChatEntity.toDomain() = Chat(
        id = id,
        title = title,
        modelId = modelId,
        projectId = projectId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned,
        isArchived = isArchived,
    )

    private fun Chat.toEntity() = ChatEntity(
        id = id,
        title = title,
        modelId = modelId,
        projectId = projectId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPinned = isPinned,
        isArchived = isArchived,
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        role = if (role == "USER") Message.Role.USER else Message.Role.AI,
        content = content,
        timestamp = timestamp,
        isStreaming = isStreaming,
    )

    private fun Message.toEntity(chatId: String) = MessageEntity(
        id = id,
        chatId = chatId,
        role = if (role == Message.Role.USER) "USER" else "AI",
        content = content,
        timestamp = timestamp,
        isStreaming = isStreaming,
    )
}
