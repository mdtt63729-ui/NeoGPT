package com.neogpt.app.domain.model

data class Chat(
    val id: String,
    val title: String = "New Chat",
    val messages: List<Message> = emptyList(),
    val modelId: String = "gemini-flash",
    val projectId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
)
