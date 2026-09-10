package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class UrlTool : Tool {
    override val name = "url"
    override val description = "Fetch content from a URL"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val url = params["url"] ?: return ToolResult(name, false, "", "Missing 'url' param")
        // TODO: Implement URL fetching
        return ToolResult(name, true, "Content fetched from: $url")
    }
}
