package com.neogpt.app.agent

class CheckpointManager {
    private val checkpoints = mutableMapOf<String, Long>()

    fun createCheckpoint(taskId: String): String {
        require(taskId.isNotBlank()) { "taskId cannot be blank" }
        val checkpointId = "cp_${System.currentTimeMillis()}"
        checkpoints[checkpointId] = System.currentTimeMillis()
        return checkpointId
    }

    fun restoreCheckpoint(checkpointId: String): Long {
        return checkpoints[checkpointId] ?: throw IllegalArgumentException("Checkpoint not found: $checkpointId")
    }
}
