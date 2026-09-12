package com.neogpt.app.ui.navigation

import android.net.Uri

object NeoRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val API_SETUP = "api_setup"
    const val CHAT = "chat/{chatId}?model={model}&prompt={prompt}&attachmentUri={attachmentUri}&attachmentName={attachmentName}&attachmentMime={attachmentMime}"
    const val LIVE = "live/{model}"
    const val SEARCH = "search"
    const val PROJECTS = "projects"
    const val PROJECT_DETAIL = "project/{projectId}"
    const val CUSTOM_AI = "custom_ai"
    const val RESEARCH = "research"
    const val CODE = "code"
    const val CANVAS = "canvas"
    const val FILES = "files"
    const val TASKS = "tasks"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"
    const val ADMIN_LOGIN = "admin_login"

    fun chat(chatId: String = "new", model: String = "neo:neo-4.1-alpha", prompt: String = "", attachmentUri: String = "", attachmentName: String = "", attachmentMime: String = "") =
        "chat/${Uri.encode(chatId)}?model=${Uri.encode(model)}&prompt=${Uri.encode(prompt)}&attachmentUri=${Uri.encode(attachmentUri)}&attachmentName=${Uri.encode(attachmentName)}&attachmentMime=${Uri.encode(attachmentMime)}"
    fun live(model: String = "gemini:gemini-3.8-flash") = "live/${Uri.encode(model)}"
    fun projectDetail(projectId: String) = "project/${Uri.encode(projectId)}"
}
