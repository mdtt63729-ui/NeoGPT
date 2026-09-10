package com.neogpt.app.coding

data class CodeCheckpoint(
    val id: String,
    val taskId: String,
    val timestamp: Long,
    val description: String,
    val fileSnapshots: Map<String, String>,
)
