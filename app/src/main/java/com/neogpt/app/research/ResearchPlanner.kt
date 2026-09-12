package com.neogpt.app.research

class ResearchPlanner {
    fun createPlan(goal: String): List<String> {
        return listOf(
            "Identify relevant information about: $goal",
            "Search authoritative sources",
            "Compare findings across sources",
            "Verify claims with citations",
            "Generate comprehensive report",
        )
    }
}
