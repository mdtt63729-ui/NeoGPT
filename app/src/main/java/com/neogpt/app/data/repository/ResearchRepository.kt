package com.neogpt.app.data.repository

import com.neogpt.app.domain.model.Research
import com.neogpt.app.domain.model.ResearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResearchRepository {
    private val _researchList = MutableStateFlow<List<Research>>(emptyList())
    val researchList: StateFlow<List<Research>> = _researchList.asStateFlow()

    fun createResearch(goal: String): Research {
        val research = Research(
            id = System.currentTimeMillis().toString(),
            goal = goal,
            plan = listOf(
                "Identify relevant information",
                "Search sources",
                "Compare findings",
                "Verify claims",
                "Generate report",
            ),
        )
        _researchList.value = _researchList.value + research
        return research
    }

    fun updateResearchState(id: String, state: ResearchState) {
        _researchList.value = _researchList.value.map {
            if (it.id == id) it.copy(state = state) else it
        }
    }
}
