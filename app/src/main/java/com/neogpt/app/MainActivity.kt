package com.neogpt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.neogpt.app.ui.navigation.NeoNavGraph
import com.neogpt.app.ui.screens.drawer.NeoDrawer
import com.neogpt.app.ui.theme.NeoGPTTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeoGPTTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NeoDrawer(
                            onNavigate = { route ->
                                scope.launch { drawerState.close() }
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            },
                            onClose = { scope.launch { drawerState.close() } },
                        )
                    },
                ) {
                    NeoNavGraph(
                        navController = navController,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                    )
                }
            }
        }
    }
}
