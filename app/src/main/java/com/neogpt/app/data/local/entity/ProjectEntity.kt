package com.neogpt.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val instructions: String,
    val createdAt: Long,
    val lastActivity: Long,
)
