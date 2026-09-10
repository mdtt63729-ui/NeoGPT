package com.neogpt.app.domain.model

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val speedBadge: String,
    val contextWindow: String,
    val capabilities: List<String> = emptyList(),
    val isAvailable: Boolean = true,
)
