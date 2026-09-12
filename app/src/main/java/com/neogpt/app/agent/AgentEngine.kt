package com.neogpt.app.agent

import com.neogpt.app.tools.ToolExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AgentEngine(
    private val planner: AgentPlanner,
    private val executor: AgentExecutor,
) {
    fun run(task: String): Flow<AgentState> = flow {
        emit(AgentState.PLANNING)
        val plan = planner.plan(task)
        emit(AgentState.EXECUTING)
        executor.execute(plan)
        emit(AgentState.COMPLETED)
    }
}
