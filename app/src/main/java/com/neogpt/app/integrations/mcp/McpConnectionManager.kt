package com.neogpt.app.integrations.mcp

class McpConnectionManager {
    private val servers = mutableMapOf<String, McpServer>()

    fun registerServer(server: McpServer) {
        servers[server.name] = server
    }

    fun getConnectedServers(): List<McpServer> = servers.values.filter { it.isConnected }
    fun getAvailableTools(): List<McpTool> = servers.values.flatMap { it.tools }
}
