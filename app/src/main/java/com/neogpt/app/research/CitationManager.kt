package com.neogpt.app.research

import com.neogpt.app.domain.model.ResearchSource


class CitationManager {
    private val citations = mutableMapOf<String, ResearchSource>()

    fun addCitation(id: String, source: ResearchSource) {
        citations[id] = source
    }

    fun formatCitations(): String {
        return citations.entries.mapIndexed { index, (id, source) ->
            "[${index + 1}] ${source.title} - ${source.url}"
        }.joinToString("\n")
    }
}

