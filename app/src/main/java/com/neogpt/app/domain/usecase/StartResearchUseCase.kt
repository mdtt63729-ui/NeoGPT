package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.ResearchRepository
import com.neogpt.app.domain.model.Research

class StartResearchUseCase(
    private val researchRepository: ResearchRepository,
) {
    operator fun invoke(goal: String): Research {
        return researchRepository.createResearch(goal)
    }
}
