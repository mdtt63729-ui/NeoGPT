package com.neogpt.app.coding

class TestManager {
    fun runTests(projectPath: String): TestResult {
        // TODO: Execute test command
        return TestResult(passed = 0, failed = 0, output = "No tests found")
    }
}

data class TestResult(val passed: Int, val failed: Int, val output: String)
