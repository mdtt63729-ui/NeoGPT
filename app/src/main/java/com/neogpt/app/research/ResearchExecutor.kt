package com.neogpt.app.research

import com.neogpt.app.domain.model.ResearchSource
import com.neogpt.app.tools.SearchTool

class ResearchExecutor(
    private val searchTool: SearchTool = SearchTool(),
) {
    suspend fun execute(plan: List<String>): List<ResearchSource> {
        val sources = mutableListOf<ResearchSource>()
        for ((index, step) in plan.withIndex()) {
            val result = searchTool.execute(mapOf("query" to step))
            if (result.success && result.output.isNotBlank()) {
                sources += ResearchSource(
                    id = "source_${index + 1}",
                    title = step,
                    url = "",
                    snippet = result.output.take(500),
                )
            }
        }
        return sources
    }
}
