package com.neogpt.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neogpt.app.ui.navigation.NeoNavGraph
import com.neogpt.app.ui.navigation.NeoRoutes
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
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route
                var exitDialog by remember { mutableStateOf(false) }
                var lastBackAt by remember { mutableLongStateOf(0L) }

                DisposableEffect(currentRoute, drawerState.isOpen, exitDialog) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            when {
                                exitDialog -> exitDialog = false
                                drawerState.isOpen -> scope.launch { drawerState.close() }
                                currentRoute == NeoRoutes.HOME -> {
                                    val now = System.currentTimeMillis()
                                    if (now - lastBackAt <= 1800L) {
                                        exitDialog = true
                                        lastBackAt = 0L
                                    } else {
                                        lastBackAt = now
                                        Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> navController.popBackStack()
                            }
                        }
                    }
                    onBackPressedDispatcher.addCallback(this@MainActivity, callback)
                    onDispose { callback.remove() }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NeoDrawer(
                            onNavigate = { route ->
                                scope.launch { drawerState.close() }
                                navController.navigate(route) { launchSingleTop = true }
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

                if (exitDialog) {
                    AlertDialog(
                        onDismissRequest = { exitDialog = false },
                        title = { Text("Exit Neo GPT?") },
                        text = { Text("Are you sure you want to close the app?") },
                        confirmButton = {
                            TextButton(onClick = { exitDialog = false; finish() }) { Text("Exit") }
                        },
                        dismissButton = {
                            TextButton(onClick = { exitDialog = false }) { Text("Cancel") }
                        },
                    )
                }
            }
        }
    }
}
