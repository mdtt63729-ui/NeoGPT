package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult
import java.io.File

class FileTool : Tool {
    override val name = "file"
    override val description = "Read and write text files available to the app"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val action = params["action"] ?: return ToolResult(name, false, "", "Missing 'action' parameter")
        val path = params["path"] ?: return ToolResult(name, false, "", "Missing 'path' parameter")
        return runCatching {
            val file = File(path).canonicalFile
            when (action.lowercase()) {
                "read" -> {
                    if (!file.exists()) return@runCatching ToolResult(name, false, "", "File does not exist")
                    ToolResult(name, true, file.readText())
                }
                "write" -> {
                    val content = params["content"] ?: return@runCatching ToolResult(name, false, "", "Missing 'content' parameter")
                    file.parentFile?.mkdirs()
                    file.writeText(content)
                    ToolResult(name, true, "Wrote ${file.length()} bytes to ${file.path}")
                }
                "exists" -> ToolResult(name, true, file.exists().toString())
                else -> ToolResult(name, false, "", "Unsupported action: $action")
            }
        }.getOrElse { ToolResult(name, false, "", it.message ?: "File operation failed") }
    }
}
