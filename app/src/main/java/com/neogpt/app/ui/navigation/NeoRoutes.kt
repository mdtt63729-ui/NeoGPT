package com.neogpt.app.ui.navigation

object NeoRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val CHAT = "chat/{chatId}"
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

    fun chat(chatId: String = "new") = "chat/$chatId"
    fun projectDetail(projectId: String) = "project/$projectId"
}
