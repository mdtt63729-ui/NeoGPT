package com.neogpt.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neogpt.app.ui.screens.canvas.CanvasScreen
import com.neogpt.app.ui.screens.chat.ChatScreen
import com.neogpt.app.ui.screens.chat.ChatHistoryScreen
import com.neogpt.app.ui.screens.code.CodeScreen
import com.neogpt.app.ui.screens.customai.CustomAIScreen
import com.neogpt.app.ui.screens.files.FilesScreen
import com.neogpt.app.ui.screens.home.HomeScreen
import com.neogpt.app.ui.screens.live.LiveConversationScreen
import com.neogpt.app.ui.screens.notifications.NotificationsScreen
import com.neogpt.app.ui.screens.projects.ProjectDetailScreen
import com.neogpt.app.ui.screens.projects.ProjectsScreen
import com.neogpt.app.ui.screens.research.ResearchScreen
import com.neogpt.app.ui.screens.search.SearchScreen
import com.neogpt.app.ui.screens.settings.SettingsScreen
import com.neogpt.app.ui.screens.settings.AdminLoginScreen
import com.neogpt.app.ui.screens.setup.ApiSetupScreen
import com.neogpt.app.ui.screens.vibeee.VibeeeScreen

@Composable
fun NeoNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = NeoRoutes.HOME,
        enterTransition = { iosEnter() },
        exitTransition = { iosExit() },
        popEnterTransition = { iosPopEnter() },
        popExitTransition = { iosPopExit() },
    ) {
        composable(NeoRoutes.API_SETUP, enterTransition = { iosEnter() }) {
            ApiSetupScreen(
                onComplete = {
                    navController.navigate(NeoRoutes.HOME) {
                        popUpTo(NeoRoutes.API_SETUP) { inclusive = true }
                    }
                },
            )
        }
        composable(NeoRoutes.HOME, enterTransition = { iosEnter() }) {
            HomeScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenChat = { model, prompt, uri, name, mime, size -> navController.navigate(NeoRoutes.chat(model = model, prompt = prompt, attachmentUri = uri, attachmentName = name, attachmentMime = mime, attachmentSize = size)) },
                onOpenLive = { navController.navigate(NeoRoutes.live()) },
            )
        }
        composable(
            NeoRoutes.CHAT,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType; defaultValue = "neo:neo-4.1-alpha" },
                navArgument("prompt") { type = NavType.StringType; defaultValue = "" },
                navArgument("attachmentUri") { type = NavType.StringType; defaultValue = "" },
                navArgument("attachmentName") { type = NavType.StringType; defaultValue = "" },
                navArgument("attachmentMime") { type = NavType.StringType; defaultValue = "" },
                navArgument("attachmentSize") { type = NavType.LongType; defaultValue = 0L },
            ),
            enterTransition = { slideUpEnter() },
            exitTransition = { slideRightExit() },
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "new"
            val model = backStackEntry.arguments?.getString("model") ?: "neo:neo-4.1-alpha"
            val prompt = backStackEntry.arguments?.getString("prompt").orEmpty()
            val attachmentUri = backStackEntry.arguments?.getString("attachmentUri").orEmpty()
            val attachmentName = backStackEntry.arguments?.getString("attachmentName").orEmpty()
            val attachmentMime = backStackEntry.arguments?.getString("attachmentMime").orEmpty()
            val attachmentSize = backStackEntry.arguments?.getLong("attachmentSize") ?: 0L
            ChatScreen(
                chatId = chatId,
                modelId = model,
                initialPrompt = prompt,
                initialAttachmentUri = attachmentUri,
                initialAttachmentName = attachmentName,
                initialAttachmentMime = attachmentMime,
                initialAttachmentSize = attachmentSize,
                onBack = { navController.popBackStack() },
                onOpenDrawer = onOpenDrawer,
                onOpenLive = { navController.navigate(NeoRoutes.live(model)) },
            )
        }
        composable(NeoRoutes.LIVE, arguments = listOf(navArgument("model") { type = NavType.StringType; defaultValue = "gemini:gemini-3.8-flash" }), enterTransition = { slideUpEnter() }) { entry ->
            LiveConversationScreen(
                modelId = entry.arguments?.getString("model") ?: "gemini:gemini-3.8-flash",
                onBack = { navController.popBackStack() },
            )
        }
        composable(NeoRoutes.SEARCH, enterTransition = { slideUpEnter() }) {
            SearchScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.CHAT_HISTORY, enterTransition = { slideHorizontalEnter() }) {
            ChatHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenChat = { chatId, modelId -> navController.navigate(NeoRoutes.chat(chatId = chatId, model = modelId)) },
            )
        }
        composable(NeoRoutes.PROJECTS, enterTransition = { slideHorizontalEnter() }) {
            ProjectsScreen(
                onBack = { navController.popBackStack() },
                onProjectClick = { id -> navController.navigate(NeoRoutes.projectDetail(id)) },
                onNewProject = { navController.navigate(NeoRoutes.projectDetail("new")) },
            )
        }
        composable(NeoRoutes.PROJECT_DETAIL, arguments = listOf(navArgument("projectId") { type = NavType.StringType }), enterTransition = { slideHorizontalEnter() }) { entry ->
            ProjectDetailScreen(
                projectId = entry.arguments?.getString("projectId") ?: "new",
                onBack = { navController.popBackStack() },
            )
        }
        composable(NeoRoutes.CUSTOM_AI, enterTransition = { slideHorizontalEnter() }) { CustomAIScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.RESEARCH, enterTransition = { slideUpEnter() }) { ResearchScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.CODE, enterTransition = { slideHorizontalEnter() }) { CodeScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.CANVAS, enterTransition = { slideHorizontalEnter() }) { CanvasScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.FILES, enterTransition = { slideUpEnter() }) { FilesScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.VIBEEE, enterTransition = { slideHorizontalEnter() }) { VibeeeScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.NOTIFICATIONS, enterTransition = { slideUpEnter() }) { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.SETTINGS, enterTransition = { slideUpEnter() }) {
            SettingsScreen(onBack = { navController.popBackStack() }, onAdminLogin = { navController.navigate(NeoRoutes.ADMIN_LOGIN) })
        }
        composable(NeoRoutes.ADMIN_LOGIN, enterTransition = { slideUpEnter() }) {
            AdminLoginScreen(onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() })
        }
    }
}
