package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult
import okhttp3.OkHttpClient
import okhttp3.Request

class UrlTool : Tool {
    override val name = "url"
    override val description = "Fetch text content from an HTTP(S) URL"

    private val client = OkHttpClient()

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val url = params["url"] ?: return ToolResult(name, false, "", "Missing 'url' parameter")
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return ToolResult(name, false, "", "Only HTTP(S) URLs are supported")
        }
        return runCatching {
            val response = client.newCall(Request.Builder().url(url).get().build()).execute()
            response.use {
                if (!it.isSuccessful) ToolResult(name, false, "", "HTTP ${it.code}")
                else ToolResult(name, true, it.body?.string().orEmpty())
            }
        }.getOrElse { ToolResult(name, false, "", it.message ?: "Request failed") }
    }
}
