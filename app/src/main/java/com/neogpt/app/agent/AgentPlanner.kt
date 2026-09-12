package com.neogpt.app.agent

class AgentPlanner {
    fun plan(task: String): List<AgentStep> {
        require(task.isNotBlank()) { "Task cannot be blank" }
        return listOf(
            AgentStep("1", "Analyze request"),
            AgentStep("2", "Gather resources"),
            AgentStep("3", "Execute task"),
            AgentStep("4", "Review results"),
        )
    }
}
