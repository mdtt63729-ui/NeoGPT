package com.neogpt.app.research

import com.neogpt.app.domain.model.ResearchSource

class SourceManager {
    private val sources = mutableListOf<ResearchSource>()

    fun addSource(source: ResearchSource) { sources.add(source) }
    fun getSources(): List<ResearchSource> = sources.toList()
    fun clearSources() { sources.clear() }
}
