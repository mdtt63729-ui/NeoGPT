package com.neogpt.app.coding

data class CodeProject(
    val id: String,
    val name: String,
    val rootPath: String,
    val files: List<CodeFile> = emptyList(),
)
