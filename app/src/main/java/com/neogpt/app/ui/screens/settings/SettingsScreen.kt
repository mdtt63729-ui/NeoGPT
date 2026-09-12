package com.neogpt.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val storage = remember { SecureStorage(context) }
    var gemini by remember { mutableStateOf(storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()) }
    var openRouter by remember { mutableStateOf(storage.getProviderKey(AiProvider.OPENROUTER.id).orEmpty()) }
    var nvidia by remember { mutableStateOf(storage.getProviderKey(AiProvider.NVIDIA.id).orEmpty()) }
    var showGemini by remember { mutableStateOf(false) }
    var showOpenRouter by remember { mutableStateOf(false) }
    var showNvidia by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<String?>(null) }

    NeoPage("Settings", onBack) {
        NeoSectionTitle("AI providers", "Connect Gemini, OpenRouter and NVIDIA NIM. Each key is encrypted with Android Keystore and stays on this device.")
        ProviderSettingCard(AiProvider.GEMINI, gemini, { gemini = it }, showGemini, { showGemini = !showGemini }, saved == "gemini", { storage.clearProviderKey(AiProvider.GEMINI.id); gemini = ""; saved = null }) {
            storage.saveProviderKey(AiProvider.GEMINI.id, gemini); saved = "gemini"
        }
        Spacer(Modifier.height(NeoSpacing.sm))
        ProviderSettingCard(AiProvider.OPENROUTER, openRouter, { openRouter = it }, showOpenRouter, { showOpenRouter = !showOpenRouter }, saved == "openrouter", { storage.clearProviderKey(AiProvider.OPENROUTER.id); openRouter = ""; saved = null }) {
            storage.saveProviderKey(AiProvider.OPENROUTER.id, openRouter); saved = "openrouter"
        }
        Spacer(Modifier.height(NeoSpacing.sm))
        ProviderSettingCard(AiProvider.NVIDIA, nvidia, { nvidia = it }, showNvidia, { showNvidia = !showNvidia }, saved == "nvidia", { storage.clearProviderKey(AiProvider.NVIDIA.id); nvidia = ""; saved = null }) {
            storage.saveProviderKey(AiProvider.NVIDIA.id, nvidia); saved = "nvidia"
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        NeoSectionTitle("Model routing", "The Home model picker shows models for the providers you have configured. Gemini uses its native API; OpenRouter and NVIDIA use their OpenAI-compatible chat APIs.")
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Gemini 3 family") },
                supportingContent = { Text("3.8 Flash, 3.7 Flash, 3.5 Flash-Lite, 3.6 Flash and 3.5 Flash") },
                leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
            )
            ListItem(
                headlineContent = { Text("OpenRouter") },
                supportingContent = { Text("Configured catalog models and OpenRouter streaming") },
                leadingContent = { Icon(Icons.Rounded.Hub, null) },
            )
            ListItem(
                headlineContent = { Text("NVIDIA NIM") },
                supportingContent = { Text("NVIDIA catalog identifiers with chat-capable models enabled") },
                leadingContent = { Icon(Icons.Rounded.Memory, null) },
            )
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        NeoSectionTitle("Appearance")
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("System theme") },
                supportingContent = { Text("Neo GPT follows your device's light or dark mode and Material colors.") },
                leadingContent = { Icon(Icons.Rounded.Palette, null) },
            )
        }
        Spacer(Modifier.height(NeoSpacing.md))
        NeoFeatureCard(Icons.Rounded.Security, "Privacy & security", "Provider API keys stay in encrypted Android Keystore-backed storage.", {})
        Spacer(Modifier.height(NeoSpacing.sm))
        NeoFeatureCard(Icons.Rounded.Info, "About Neo GPT", "Version 1.1.0 • Material 3 • Multi-provider AI", {})
    }
}

@Composable
private fun ProviderSettingCard(
    provider: AiProvider,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
    isSaved: Boolean,
    onClear: () -> Unit,
    onSave: () -> Unit,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, null)
                Column(Modifier.weight(1f)) {
                    Text(provider.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(if (value.isBlank()) "Not connected" else if (isSaved) "Saved securely" else "Configured", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(NeoSpacing.sm))
            OutlinedTextField(
                value = value,
                onValueChange = { onValueChange(it); },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("${provider.displayName} API key") },
                visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = onToggle) { Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null) } },
                shape = NeoShapes.large,
            )
            Spacer(Modifier.height(NeoSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                Button(onClick = onSave, enabled = value.isNotBlank(), shape = NeoShapes.pill) { Text(if (isSaved) "Saved" else "Save key") }
                if (value.isNotBlank()) {
                    OutlinedButton(onClick = onClear, shape = NeoShapes.pill) { Text("Clear") }
                }
            }
        }
    }
}
