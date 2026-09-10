package com.neogpt.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.neogpt.app.ui.screens.splash.SplashScreen
import com.neogpt.app.ui.screens.home.HomeScreen
import com.neogpt.app.ui.screens.chat.ChatScreen
import com.neogpt.app.ui.screens.search.SearchScreen
import com.neogpt.app.ui.screens.projects.ProjectsScreen
import com.neogpt.app.ui.screens.projects.ProjectDetailScreen
import com.neogpt.app.ui.screens.customai.CustomAIScreen
import com.neogpt.app.ui.screens.research.ResearchScreen
import com.neogpt.app.ui.screens.code.CodeScreen
import com.neogpt.app.ui.screens.canvas.CanvasScreen
import com.neogpt.app.ui.screens.files.FilesScreen
import com.neogpt.app.ui.screens.tasks.TasksScreen
import com.neogpt.app.ui.screens.notifications.NotificationsScreen
import com.neogpt.app.ui.screens.settings.SettingsScreen

@Composable
fun NeoNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = NeoRoutes.SPLASH,
    ) {
        composable(NeoRoutes.SPLASH, exitTransition = { fadeOut() }) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(NeoRoutes.HOME) {
                        popUpTo(NeoRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(NeoRoutes.HOME, enterTransition = { fadeIn() }) {
            HomeScreen(
                onOpenDrawer = onOpenDrawer,
                onOpenChat = { navController.navigate(NeoRoutes.chat()) },
                onOpenSearch = { navController.navigate(NeoRoutes.SEARCH) },
                onOpenSettings = { navController.navigate(NeoRoutes.SETTINGS) },
                onOpenProjects = { navController.navigate(NeoRoutes.PROJECTS) },
                onOpenCustomAI = { navController.navigate(NeoRoutes.CUSTOM_AI) },
            )
        }
        composable(
            NeoRoutes.CHAT,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
            enterTransition = { slideUpEnter() },
            exitTransition = { slideRightExit() },
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "new"
            ChatScreen(
                chatId = chatId,
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
        composable(
            NeoRoutes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
            enterTransition = { slideHorizontalEnter() },
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: "new"
            ProjectDetailScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(NeoRoutes.CUSTOM_AI, enterTransition = { slideHorizontalEnter() }) {
            CustomAIScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.RESEARCH, enterTransition = { slideUpEnter() }) {
            ResearchScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.CODE, enterTransition = { slideHorizontalEnter() }) {
            CodeScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.CANVAS, enterTransition = { slideHorizontalEnter() }) {
            CanvasScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.FILES, enterTransition = { slideUpEnter() }) {
            FilesScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.TASKS, enterTransition = { slideUpEnter() }) {
            TasksScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.NOTIFICATIONS, enterTransition = { slideUpEnter() }) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(NeoRoutes.SETTINGS, enterTransition = { slideUpEnter() }) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
