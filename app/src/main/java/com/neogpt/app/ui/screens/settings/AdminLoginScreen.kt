package com.neogpt.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.security.AdminAuth
import com.neogpt.app.ui.components.NeoPage
import com.neogpt.app.ui.theme.NeoShapes

@Composable
fun AdminLoginScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { AdminAuth(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    NeoPage("Admin access", onBack) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = NeoShapes.large,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = .16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .35f)),
                ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AdminPanelSettings, null, Modifier.size(38.dp), tint = MaterialTheme.colorScheme.primary) } }
                Spacer(Modifier.height(20.dp))
                Text("Admin login", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text("Sign in locally to unlock Neo 4.1 Alpha.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(email, { email = it; error = false }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, leadingIcon = { Icon(Icons.Rounded.AdminPanelSettings, null) }, shape = NeoShapes.large)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(password, { password = it; error = false }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Password") }, leadingIcon = { Icon(Icons.Rounded.Lock, null) }, visualTransformation = PasswordVisualTransformation(), isError = error, shape = NeoShapes.large)
                if (error) {
                    Spacer(Modifier.height(8.dp)); Text("Incorrect admin credentials.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(18.dp))
                NeoButton("Login", {
                    if (auth.login(email, password)) onSuccess() else error = true
                }, Modifier.fillMaxWidth(), icon = Icons.Rounded.AdminPanelSettings)
            }
        }
    }
}
