package com.neogpt.app.agent

import com.neogpt.app.tools.ToolExecutor

class AgentExecutor(private val toolExecutor: ToolExecutor) {
    suspend fun execute(steps: List<AgentStep>) {
        steps.forEach { step ->
            // TODO: Execute each step using tools
        }
    }
}
