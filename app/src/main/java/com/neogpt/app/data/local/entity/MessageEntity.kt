package com.neogpt.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String,
    val content: String,
    val attachmentsJson: String = "[]",
    val timestamp: Long,
    val isStreaming: Boolean = false,
)
