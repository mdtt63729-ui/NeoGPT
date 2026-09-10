package com.neogpt.app.integrations.mcp

data class McpTool(
    val name: String,
    val description: String,
    val parameters: Map<String, String>,
)
