package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class FileTool : Tool {
    override val name = "file"
    override val description = "Read and write files"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val action = params["action"] ?: return ToolResult(name, false, "", "Missing 'action' param")
        val path = params["path"] ?: return ToolResult(name, false, "", "Missing 'path' param")
        // TODO: Implement file operations
        return ToolResult(name, true, "File $action on $path")
    }
}
