package com.neogpt.app.ui.navigation

import android.net.Uri

object NeoRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val API_SETUP = "api_setup"
    const val CHAT = "chat/{chatId}?model={model}&prompt={prompt}"
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

    fun chat(chatId: String = "new", model: String = "gemini-2.5-flash", prompt: String = "") =
        "chat/${Uri.encode(chatId)}?model=${Uri.encode(model)}&prompt=${Uri.encode(prompt)}"
    fun projectDetail(projectId: String) = "project/${Uri.encode(projectId)}"
}
