package com.neogpt.app.ui.navigation

import androidx.navigation.NavHostController

fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
