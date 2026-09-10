package com.neogpt.app.research

import com.neogpt.app.domain.model.Research
import com.neogpt.app.domain.model.ResearchState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ResearchEngine(
    private val planner: ResearchPlanner,
    private val executor: ResearchExecutor,
    private val reportGenerator: ResearchReportGenerator,
) {
    fun run(goal: String): Flow<Research> = flow {
        val plan = planner.createPlan(goal)
        val sources = executor.execute(plan)
        val report = reportGenerator.generate(goal, sources)
        emit(Research(
            id = System.currentTimeMillis().toString(),
            goal = goal,
            plan = plan,
            sources = sources,
            report = report,
            state = ResearchState.COMPLETED,
        ))
    }
}
