package com.neogpt.app.domain.model

data class Task(
    val id: String,
    val name: String,
    val schedule: String = "",
    val state: TaskState = TaskState.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRun: Long? = null,
)

enum class TaskState { SCHEDULED, RUNNING, COMPLETED, FAILED, PAUSED }
