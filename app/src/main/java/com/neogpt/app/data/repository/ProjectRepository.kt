package com.neogpt.app.data.repository

import com.neogpt.app.data.local.dao.ProjectDao
import com.neogpt.app.data.local.entity.ProjectEntity
import com.neogpt.app.domain.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProjectRepository(private val projectDao: ProjectDao) {
    fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun createProject(name: String, description: String): Project {
        val project = Project(
            id = System.currentTimeMillis().toString(),
            name = name,
            description = description,
        )
        projectDao.insertProject(project.toEntity())
        return project
    }

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        icon = icon,
        instructions = instructions,
        createdAt = createdAt,
        lastActivity = lastActivity,
    )

    private fun Project.toEntity() = ProjectEntity(
        id = id,
        name = name,
        description = description,
        icon = icon,
        instructions = instructions,
        createdAt = createdAt,
        lastActivity = lastActivity,
    )
}
