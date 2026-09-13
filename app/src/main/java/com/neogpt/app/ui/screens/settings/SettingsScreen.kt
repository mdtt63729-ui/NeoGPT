package com.neogpt.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.security.AdminAuth
import com.neogpt.app.settings.AppSettings
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun SettingsScreen(onBack: () -> Unit, onAdminLogin: () -> Unit = {}) {
    val context = LocalContext.current
    val storage = remember { SecureStorage(context) }
    val settings = remember { AppSettings.get(context) }
    val ui by settings.state.collectAsState()
    var gemini by remember { mutableStateOf(storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()) }
    var openRouter by remember { mutableStateOf(storage.getProviderKey(AiProvider.OPENROUTER.id).orEmpty()) }
    var nvidia by remember { mutableStateOf(storage.getProviderKey(AiProvider.NVIDIA.id).orEmpty()) }
    var showGemini by remember { mutableStateOf(false) }
    var showOpenRouter by remember { mutableStateOf(false) }
    var showNvidia by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<String?>(null) }
    val adminAuth = remember { AdminAuth(context) }
    var adminLoggedIn by remember { mutableStateOf(adminAuth.isLoggedIn()) }

    NeoPage("Settings", onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = NeoSpacing.xxl)) {
        NeoSectionTitle("Appearance", "Changes apply instantly across the app.")
        SettingSwitchCard(Icons.Rounded.WaterDrop, "Enable liquid glass design", "Use translucent glass surfaces, depth, highlights and press feedback across Neo GPT.", ui.liquidGlass) { settings.setLiquidGlass(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        SettingSwitchCard(Icons.Rounded.AutoAwesomeMotion, "Smooth animations", "Keep Neo GPT's motion, press and transition animations enabled.", ui.animations) { settings.setAnimations(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        SettingSwitchCard(Icons.Rounded.Palette, "Dynamic Material colors", "Use Android dynamic colors when available. Turn off for Neo GPT's fixed palette.", ui.dynamicColor) { settings.setDynamicColor(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        ThemeModeCard(ui.themeMode) { settings.setThemeMode(it) }

        NeoSectionTitle("Chat", "Controls that change live chat behavior immediately.")
        SettingSwitchCard(Icons.Rounded.VerticalAlignBottom, "Auto-scroll AI responses", "Keep the newest streamed AI content in view unless you scroll upward yourself.", ui.autoScroll) { settings.setAutoScroll(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        SettingSwitchCard(Icons.Rounded.Send, "Enter to send", "Pressing the keyboard Send action sends the current message. Turn off to keep Enter as a newline.", ui.enterToSend) { settings.setEnterToSend(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        SettingSwitchCard(Icons.Rounded.Vibration, "Haptic feedback", "Use subtle Android haptics for supported primary interactions.", ui.haptics) { settings.setHaptics(it) }
        Spacer(Modifier.height(NeoSpacing.sm))
        SettingSwitchCard(Icons.Rounded.Schedule, "Show message timestamps", "Display message time metadata where supported by the conversation UI.", ui.showTimestamps) { settings.setShowTimestamps(it) }

        NeoSectionTitle("Admin access", "Neo 4.1 Alpha is hidden until local admin login is completed.")
        if (adminLoggedIn) {
            NeoFeatureCard(Icons.Rounded.AdminPanelSettings, "Admin mode enabled", "Neo 4.1 Alpha is available in the model picker.", {
                adminAuth.logout(); adminLoggedIn = false
            })
        } else {
            NeoFeatureCard(Icons.Rounded.Lock, "Admin login", "Login locally to unlock Neo 4.1 Alpha. No Firebase or backend is used.", onAdminLogin)
        }

        NeoSectionTitle("AI providers", "Connect Gemini, OpenRouter and NVIDIA NIM. Keys remain encrypted on this device.")
        ProviderSettingCard(AiProvider.GEMINI, gemini, { gemini = it }, showGemini, { showGemini = !showGemini }, saved == "gemini", { storage.clearProviderKey(AiProvider.GEMINI.id); gemini = ""; saved = null }) { storage.saveProviderKey(AiProvider.GEMINI.id, gemini); saved = "gemini" }
        Spacer(Modifier.height(NeoSpacing.sm))
        ProviderSettingCard(AiProvider.OPENROUTER, openRouter, { openRouter = it }, showOpenRouter, { showOpenRouter = !showOpenRouter }, saved == "openrouter", { storage.clearProviderKey(AiProvider.OPENROUTER.id); openRouter = ""; saved = null }) { storage.saveProviderKey(AiProvider.OPENROUTER.id, openRouter); saved = "openrouter" }
        Spacer(Modifier.height(NeoSpacing.sm))
        ProviderSettingCard(AiProvider.NVIDIA, nvidia, { nvidia = it }, showNvidia, { showNvidia = !showNvidia }, saved == "nvidia", { storage.clearProviderKey(AiProvider.NVIDIA.id); nvidia = ""; saved = null }) { storage.saveProviderKey(AiProvider.NVIDIA.id, nvidia); saved = "nvidia" }

        NeoSectionTitle("Model routing", "Configured providers appear in the model picker. Admin-only Neo 4.1 Alpha is independent of provider keys.")
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(headlineContent = { Text("Gemini") }, supportingContent = { Text("Native Gemini API models") }, leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) })
            ListItem(headlineContent = { Text("OpenRouter") }, supportingContent = { Text("OpenAI-compatible streaming models") }, leadingContent = { Icon(Icons.Rounded.Hub, null) })
            ListItem(headlineContent = { Text("NVIDIA NIM") }, supportingContent = { Text("NVIDIA chat-capable catalog models") }, leadingContent = { Icon(Icons.Rounded.Memory, null) })
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        NeoFeatureCard(Icons.Rounded.Security, "Privacy & security", "Provider API keys stay in encrypted Android Keystore-backed storage.", {})
        Spacer(Modifier.height(NeoSpacing.sm))
        NeoFeatureCard(Icons.Rounded.Info, "About Neo GPT", "Version 1.4.0 • Material 3 • Liquid Glass option • Multi-provider AI", {})
        Spacer(Modifier.height(NeoSpacing.xxl))
        }
    }
}

@Composable
private fun SettingSwitchCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        ListItem(
            leadingContent = { Icon(icon, null) },
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(description, style = MaterialTheme.typography.bodySmall) },
            trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        )
    }
}

@Composable
private fun ThemeModeCard(current: AppSettings.ThemeMode, onChange: (AppSettings.ThemeMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Box {
            ListItem(
                leadingContent = { Icon(Icons.Rounded.DarkMode, null) },
                headlineContent = { Text("Theme") },
                supportingContent = { Text(current.name.lowercase().replaceFirstChar { it.uppercase() }) },
                trailingContent = { TextButton(onClick = { expanded = true }) { Text("Change") } },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                AppSettings.ThemeMode.values().forEach { mode ->
                    DropdownMenuItem(text = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { onChange(mode); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun ProviderSettingCard(provider: AiProvider, value: String, onValueChange: (String) -> Unit, visible: Boolean, onToggle: () -> Unit, isSaved: Boolean, onClear: () -> Unit, onSave: () -> Unit) {
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
            OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("${provider.displayName} API key") }, visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = onToggle) { Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null) } }, shape = NeoShapes.large)
            Spacer(Modifier.height(NeoSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                Button(onClick = onSave, enabled = value.isNotBlank(), shape = NeoShapes.pill) { Text(if (isSaved) "Saved" else "Save key") }
                if (value.isNotBlank()) OutlinedButton(onClick = onClear, shape = NeoShapes.pill) { Text("Clear") }
            }
        }
    }
}
