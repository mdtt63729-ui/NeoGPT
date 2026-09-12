package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class SearchTool : Tool {
    override val name = "search"
    override val description = "Search provider bridge; requires a configured search service"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val query = params["query"]?.trim().orEmpty()
        if (query.isBlank()) return ToolResult(name, false, "", "Missing 'query' parameter")
        return ToolResult(
            name,
            false,
            "",
            "No search provider is configured. Use Gemini with Google Search grounding for live web research.",
        )
    }
}
