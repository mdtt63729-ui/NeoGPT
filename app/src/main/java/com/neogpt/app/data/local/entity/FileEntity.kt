package com.neogpt.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val mimeType: String,
    val path: String,
    val sizeBytes: Long,
    val createdAt: Long,
)
