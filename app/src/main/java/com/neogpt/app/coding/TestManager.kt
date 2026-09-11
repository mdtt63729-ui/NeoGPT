package com.neogpt.app.coding

import java.io.File
import java.util.concurrent.TimeUnit

class TestManager {
    fun runTests(projectPath: String): TestResult {
        val root = File(projectPath)
        if (!root.isDirectory) return TestResult(0, 0, "Project directory not found: $projectPath")
        val command = if (File(root, "gradlew").exists()) listOf("./gradlew", "test") else listOf("gradle", "test")
        return runCatching {
            val process = ProcessBuilder(command).directory(root).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(10, TimeUnit.MINUTES)
            if (!finished) {
                process.destroyForcibly()
                TestResult(0, 1, "$output\nTest run timed out.")
            } else {
                TestResult(if (process.exitValue() == 0) 1 else 0, if (process.exitValue() == 0) 0 else 1, output)
            }
        }.getOrElse { TestResult(0, 1, it.message ?: "Unable to start tests") }
    }
}

data class TestResult(val passed: Int, val failed: Int, val output: String)
