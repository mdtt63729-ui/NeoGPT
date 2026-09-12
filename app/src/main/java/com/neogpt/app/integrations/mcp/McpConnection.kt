package com.neogpt.app.integrations.mcp

/** Lightweight MCP connection handle. Network transport can be added later without changing callers. */
class McpConnection(val serverUrl: String) {
    var isConnected: Boolean = true
        private set

    fun close() {
        isConnected = false
    }
}
