package com.neogpt.app.integrations.mcp

class McpClient {
    private val connections = mutableMapOf<String, McpConnection>()

    fun connect(serverUrl: String): McpConnection {
        val conn = McpConnection(serverUrl)
        connections[serverUrl] = conn
        return conn
    }

    fun disconnect(serverUrl: String) {
        connections.remove(serverUrl)
    }
}
