package com.neogpt.app.coding

import java.io.File
import java.util.concurrent.TimeUnit

class BuildManager {
    fun build(projectPath: String): BuildResult {
        val root = File(projectPath)
        if (!root.isDirectory) return BuildResult(false, "", "Project directory not found: $projectPath")
        val command = if (File(root, "gradlew").exists()) listOf("./gradlew", "assembleRelease") else listOf("gradle", "assembleRelease")
        return runCommand(root, command)
    }

    private fun runCommand(root: File, command: List<String>): BuildResult {
        return runCatching {
            val process = ProcessBuilder(command)
                .directory(root)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val finished = process.waitFor(10, TimeUnit.MINUTES)
            if (!finished) {
                process.destroyForcibly()
                BuildResult(false, output, "Build timed out")
            } else {
                BuildResult(process.exitValue() == 0, output, if (process.exitValue() == 0) null else "Build exited with code ${process.exitValue()}")
            }
        }.getOrElse { BuildResult(false, "", it.message ?: "Unable to start build") }
    }
}

data class BuildResult(val success: Boolean, val output: String, val error: String? = null)
