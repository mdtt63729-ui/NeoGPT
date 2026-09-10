package com.neogpt.app.coding

class BuildManager {
    fun build(projectPath: String): BuildResult {
        // TODO: Execute build command
        return BuildResult(success = true, output = "Build completed")
    }
}

data class BuildResult(val success: Boolean, val output: String, val error: String? = null)
