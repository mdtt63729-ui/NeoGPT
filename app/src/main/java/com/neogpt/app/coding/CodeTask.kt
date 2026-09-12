package com.neogpt.app.coding

data class CodeTask(
    val id: String,
    val description: String,
    val files: List<String> = emptyList(),
    val status: CodeTaskStatus = CodeTaskStatus.PENDING,
)

enum class CodeTaskStatus { PENDING, ANALYZING, IMPLEMENTING, TESTING, REVIEWING, COMPLETED, FAILED }
