package com.neogpt.app.domain.model

data class ToolResult(
    val toolName: String,
    val success: Boolean,
    val output: String,
    val error: String? = null,
)
