package com.neogpt.app.domain.model

data class Message(
    val id: String,
    val role: Role,
    val content: String,
    val attachments: List<Attachment> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
) {
    enum class Role { USER, AI }
}
