package com.neogpt.app.agent

class ApprovalManager {
    private val pendingApprovals = mutableMapOf<String, () -> Unit>()

    fun requestApproval(taskId: String, onApprove: () -> Unit) {
        pendingApprovals[taskId] = onApprove
    }

    fun approve(taskId: String) {
        pendingApprovals[taskId]?.invoke()
        pendingApprovals.remove(taskId)
    }

    fun reject(taskId: String) {
        pendingApprovals.remove(taskId)
    }
}
