package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.ProjectRepository
import com.neogpt.app.domain.model.Project

class CreateProjectUseCase(
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(name: String, description: String): Project {
        return projectRepository.createProject(name, description)
    }
}
