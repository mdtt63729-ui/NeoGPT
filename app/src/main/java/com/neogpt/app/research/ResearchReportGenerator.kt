package com.neogpt.app.research

import com.neogpt.app.domain.model.ResearchSource

class ResearchReportGenerator {
    fun generate(goal: String, sources: List<ResearchSource>): String {
        val sb = StringBuilder()
        sb.append("# Research Report\n\n")
        sb.append("## Executive Summary\n\n")
        sb.append("Research conducted on: $goal\n\n")
        sb.append("## Key Findings\n\n")
        sb.append("## Detailed Analysis\n\n")
        sb.append("## Limitations\n\n")
        sb.append("## Sources\n\n")
        sources.forEachIndexed { index, source ->
            sb.append("${index + 1}. ${source.title} - ${source.url}\n")
        }
        return sb.toString()
    }
}
