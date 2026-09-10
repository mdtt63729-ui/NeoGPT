package com.neogpt.app.agent

data class AgentTask(
    val id: String,
    val name: String,
    val steps: List<AgentStep>,
    val state: AgentState = AgentState.IDLE,
)

data class AgentStep(
    val id: String,
    val name: String,
    val status: StepStatus = StepStatus.PENDING,
)

enum class StepStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED }
