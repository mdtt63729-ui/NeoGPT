package com.neogpt.app.integrations.mcp

class McpPermissionManager {
    private val allowedTools = mutableSetOf<String>()

    fun allowTool(toolName: String) { allowedTools.add(toolName) }
    fun revokeTool(toolName: String) { allowedTools.remove(toolName) }
    fun isAllowed(toolName: String): Boolean = allowedTools.contains(toolName)
}
