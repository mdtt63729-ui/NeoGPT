package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()

    fun register(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]

    fun getAllTools(): List<Tool> = tools.values.toList()

    suspend fun execute(name: String, params: Map<String, String>): ToolResult {
        val tool = tools[name]
            ?: return ToolResult(name, false, "", "Tool not found: $name")
        return tool.execute(params)
    }
}
