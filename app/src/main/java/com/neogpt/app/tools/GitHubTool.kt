package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class GitHubTool : Tool {
    override val name = "github"
    override val description = "GitHub operation bridge"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val action = params["action"]?.trim().orEmpty()
        if (action.isBlank()) return ToolResult(name, false, "", "Missing 'action' parameter")
        return ToolResult(name, false, "", "GitHub credentials/provider are not configured for this tool.")
    }
}
