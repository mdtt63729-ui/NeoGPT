package com.neogpt.app.agent

import com.neogpt.app.tools.ToolExecutor

class AgentExecutor(private val toolExecutor: ToolExecutor) {
    suspend fun execute(steps: List<AgentStep>): List<AgentStep> {
        val completed = mutableListOf<AgentStep>()
        for (step in steps) {
            completed += step.copy(status = StepStatus.COMPLETED)
        }
        return completed
    }
}
