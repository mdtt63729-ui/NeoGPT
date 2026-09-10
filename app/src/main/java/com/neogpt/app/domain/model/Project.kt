package com.neogpt.app.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String = "folder",
    val instructions: String = "",
    val knowledgeFiles: List<String> = emptyList(),
    val chatIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActivity: Long = System.currentTimeMillis(),
)
