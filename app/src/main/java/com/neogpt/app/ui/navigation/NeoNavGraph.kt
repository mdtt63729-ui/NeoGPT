package com.neogpt.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.screens.canvas.CanvasScreen
import com.neogpt.app.ui.screens.chat.ChatScreen
import com.neogpt.app.ui.screens.code.CodeScreen
import com.neogpt.app.ui.screens.customai.CustomAIScreen
import com.neogpt.app.ui.screens.files.FilesScreen
import com.neogpt.app.ui.screens.home.HomeScreen
import com.neogpt.app.ui.screens.notifications.NotificationsScreen
import com.neogpt.app.ui.screens.projects.ProjectDetailScreen
import com.neogpt.app.ui.screens.projects.ProjectsScreen
import com.neogpt.app.ui.screens.research.ResearchScreen
import com.neogpt.app.ui.screens.search.SearchScreen
import com.neogpt.app.ui.screens.settings.SettingsScreen
import com.neogpt.app.ui.screens.setup.ApiSetupScreen
import com.neogpt.app.ui.screens.splash.SplashScreen
import com.neogpt.app.ui.screens.tasks.TasksScreen

@Composable
fun NeoNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = NeoRoutes.SPLASH,
    ) {
        composable(NeoRoutes.SPLASH, exitTransition = { fadeOut() }) {
            SplashScreen(
                onNavigate = {
                    val next = if (SecureStorage(context).hasApiKey()) NeoRoutes.HOME else NeoRoutes.API_SETUP
                    navController.navigate(next) {
                        popUpTo(NeoRoutes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(NeoRoutes.API_SETUP, enterTransition = { fadeIn() }) {
            ApiSetupScreen(
                onComplete = {
                    navController.navigate(NeoRoutes.HOME) {
                        popUpTo(NeoRoutes.API_SETUP) { inclusive = true }
                    }
                },
            )
        }
        composable(NeoRoutes.HOME, enterTransition = { fadeIn() }) {
            HomeScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenChat = { model, prompt -> navController.navigate(NeoRoutes.chat(model = model, prompt = prompt)) },
            )
        }
        composable(
            NeoRoutes.CHAT,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("model") { type = NavType.StringType; defaultValue = "gemini-2.5-flash" },
                navArgument("prompt") { type = NavType.StringType; defaultValue = "" },
            ),
            enterTransition = { slideUpEnter() },
            exitTransition = { slideRightExit() },
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "new"
            val model = backStackEntry.arguments?.getString("model") ?: "gemini-2.5-flash"
            val prompt = backStackEntry.arguments?.getString("prompt").orEmpty()
            ChatScreen(
                chatId = chatId,
                modelId = model,
                initialPrompt = prompt,
                onBack = { navController.popBackStack() },
                onOpenDrawer = onOpenDrawer,
            )
        }
        composable(NeoRoutes.SEARCH, enterTransition = { slideUpEnter() }) {
            SearchScreen(onBack = { navController.popBackStack() })
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
        composable(NeoRoutes.TASKS, enterTransition = { slideUpEnter() }) { TasksScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.NOTIFICATIONS, enterTransition = { slideUpEnter() }) { NotificationsScreen(onBack = { navController.popBackStack() }) }
        composable(NeoRoutes.SETTINGS, enterTransition = { slideUpEnter() }) { SettingsScreen(onBack = { navController.popBackStack() }) }
    }
}
