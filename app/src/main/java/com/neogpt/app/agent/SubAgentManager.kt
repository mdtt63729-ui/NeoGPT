package com.neogpt.app.agent

class SubAgentManager {
    private val subAgents = mutableMapOf<String, AgentTask>()

    fun createSubAgent(parentId: String, name: String): AgentTask {
        val task = AgentTask(
            id = "${parentId}_${System.currentTimeMillis()}",
            name = name,
            steps = emptyList(),
        )
        subAgents[task.id] = task
        return task
    }

    fun getSubAgents(parentId: String): List<AgentTask> =
        subAgents.values.filter { it.id.startsWith(parentId) }
}
