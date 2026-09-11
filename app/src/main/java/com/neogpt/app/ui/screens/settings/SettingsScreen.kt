package com.neogpt.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val storage = remember { SecureStorage(context) }
    var apiKey by remember { mutableStateOf(storage.getApiKey().orEmpty()) }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var dynamic by remember { mutableStateOf(false) }

    NeoPage("Settings", onBack) {
        NeoSectionTitle("AI connection", "Your Gemini key is stored using Android encrypted preferences.")
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(NeoSpacing.lg)) {
                Text("Gemini API key", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(NeoSpacing.sm))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; saved = false },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { showKey = !showKey }) { Icon(if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "Show key") } },
                    placeholder = { Text("Paste your Gemini API key") },
                )
                Spacer(Modifier.height(NeoSpacing.sm))
                Text("The key is required for live AI responses.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(NeoSpacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    Button(onClick = { if (apiKey.isNotBlank()) { storage.saveApiKey(apiKey.trim()); saved = true } }, shape = NeoShapes.pill) { Text(if (saved) "Saved" else "Save key") }
                    if (storage.hasApiKey()) OutlinedButton(onClick = { storage.clearApiKey(); apiKey = ""; saved = false }, shape = NeoShapes.pill) { Text("Clear") }
                }
            }
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        NeoSectionTitle("Appearance")
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Dynamic colors") },
                supportingContent = { Text("Use your device's Material color palette.") },
                leadingContent = { Icon(Icons.Rounded.Palette, null) },
                trailingContent = { Switch(dynamic, { dynamic = it }) },
            )
        }
        Spacer(Modifier.height(NeoSpacing.md))
        NeoFeatureCard(Icons.Rounded.Security, "Privacy & security", "API keys stay in encrypted app storage.", {})
        Spacer(Modifier.height(NeoSpacing.sm))
        NeoFeatureCard(Icons.Rounded.Info, "About Neo GPT", "Version 1.0.0 • Material 3", {})
    }
}
