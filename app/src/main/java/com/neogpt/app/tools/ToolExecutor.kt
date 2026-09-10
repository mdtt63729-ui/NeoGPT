package com.neogpt.app.tools

class ToolExecutor(private val registry: ToolRegistry) {
    suspend fun execute(name: String, params: Map<String, String>) =
        registry.execute(name, params)

    fun availableTools(): List<Tool> = registry.getAllTools()
}
