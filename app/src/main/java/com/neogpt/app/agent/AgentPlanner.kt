package com.neogpt.app.agent

class AgentPlanner {
    fun plan(task: String): List<AgentStep> {
        // TODO: Use Gemini API to generate a plan
        return listOf(
            AgentStep("1", "Analyze request"),
            AgentStep("2", "Gather resources"),
            AgentStep("3", "Execute task"),
            AgentStep("4", "Review results"),
        )
    }
}
