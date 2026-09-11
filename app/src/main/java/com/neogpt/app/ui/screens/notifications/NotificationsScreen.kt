package com.neogpt.app.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.*

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    var enabled by remember { mutableStateOf(true) }
    NeoPage("Notifications", onBack) {
        NeoSectionTitle("Stay in the loop", "Control how Neo GPT keeps you informed.")
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Notifications") },
                supportingContent = { Text(if (enabled) "Enabled" else "Paused") },
                leadingContent = { Icon(Icons.Rounded.Notifications, null) },
                trailingContent = { Switch(enabled, { enabled = it }) },
            )
        }
        Spacer(Modifier.height(com.neogpt.app.ui.theme.NeoSpacing.md))
        NeoFeatureCard(Icons.Rounded.TaskAlt, "Task updates", "Get notified when an automation completes.", {}, Modifier)
        Spacer(Modifier.height(com.neogpt.app.ui.theme.NeoSpacing.sm))
        NeoFeatureCard(Icons.Rounded.AutoAwesome, "AI activity", "Receive important generation and error alerts.", {}, Modifier)
    }
}
