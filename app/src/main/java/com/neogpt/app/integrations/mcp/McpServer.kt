package com.neogpt.app.integrations.mcp

data class McpServer(
    val name: String,
    val url: String,
    val tools: List<McpTool>,
    val isConnected: Boolean = false,
)
