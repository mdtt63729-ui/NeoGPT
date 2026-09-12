package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

interface Tool {
    val name: String
    val description: String
    suspend fun execute(params: Map<String, String>): ToolResult
}
