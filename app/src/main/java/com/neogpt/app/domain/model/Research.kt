package com.neogpt.app.domain.model

data class Research(
    val id: String,
    val goal: String,
    val plan: List<String> = emptyList(),
    val sources: List<ResearchSource> = emptyList(),
    val report: String = "",
    val state: ResearchState = ResearchState.PLANNING,
    val createdAt: Long = System.currentTimeMillis(),
)

data class ResearchSource(
    val id: String,
    val title: String,
    val url: String,
    val snippet: String,
)

enum class ResearchState { PLANNING, RESEARCHING, SOURCING, REPORTING, COMPLETED, ERROR }
