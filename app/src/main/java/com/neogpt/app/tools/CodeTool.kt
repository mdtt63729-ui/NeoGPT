package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult

class CodeTool : Tool {
    override val name = "code"
    override val description = "Perform lightweight source-code analysis"

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val code = params["code"].orEmpty()
        if (code.isBlank()) return ToolResult(name, false, "", "Missing 'code' parameter")
        val lines = code.lines()
        val todos = lines.count { it.contains("TODO", ignoreCase = true) }
        val braces = code.count { it == '{' } - code.count { it == '}' }
        val report = buildString {
            appendLine("Lines: ${lines.size}")
            appendLine("Characters: ${code.length}")
            appendLine("TODO markers: $todos")
            appendLine("Brace balance: ${if (braces == 0) "balanced" else "unbalanced by $braces"}")
        }
        return ToolResult(name, true, report)
    }
}
