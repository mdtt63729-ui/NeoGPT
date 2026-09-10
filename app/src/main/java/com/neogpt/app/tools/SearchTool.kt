package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class SearchTool : Tool {
    override val name = "search"
    override val description = "Search the web for information"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val query = params["query"] ?: return ToolResult(name, false, "", "Missing 'query' param")
        // TODO: Implement actual web search via API
        return ToolResult(name, true, "Search results for: $query")
    }
}
