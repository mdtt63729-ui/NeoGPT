package com.neogpt.app.data.repository

import com.neogpt.app.data.local.dao.TaskDao
import com.neogpt.app.data.local.entity.TaskEntity
import com.neogpt.app.domain.model.Task
import com.neogpt.app.domain.model.TaskState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun createTask(name: String, schedule: String): Task {
        val task = Task(
            id = System.currentTimeMillis().toString(),
            name = name,
            schedule = schedule,
        )
        taskDao.insertTask(task.toEntity())
        return task
    }

    private fun TaskEntity.toDomain() = Task(
        id = id,
        name = name,
        schedule = schedule,
        state = TaskState.valueOf(state),
        createdAt = createdAt,
        lastRun = lastRun,
    )

    private fun Task.toEntity() = TaskEntity(
        id = id,
        name = name,
        schedule = schedule,
        state = state.name,
        createdAt = createdAt,
        lastRun = lastRun,
    )
}
