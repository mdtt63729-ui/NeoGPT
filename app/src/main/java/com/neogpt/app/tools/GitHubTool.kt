package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class GitHubTool : Tool {
    override val name = "github"
    override val description = "GitHub operations: repos, commits, PRs"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val action = params["action"] ?: return ToolResult(name, false, "", "Missing 'action' param")
        // TODO: Implement GitHub API calls
        return ToolResult(name, true, "GitHub action '$action' completed")
    }
}
