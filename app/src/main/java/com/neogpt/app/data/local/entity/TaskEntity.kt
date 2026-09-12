package com.neogpt.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val schedule: String,
    val state: String,
    val createdAt: Long,
    val lastRun: Long?,
)
