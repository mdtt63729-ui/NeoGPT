package com.neogpt.app.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.security.AdminAuth
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.settings.AppSettings
import com.neogpt.app.settings.SystemPromptStore
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

private enum class SettingsCategory(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    APPEARANCE("Appearance", "Theme, colors and motion", Icons.Rounded.Palette),
    CHAT("Chat & composer", "Input, voice and response controls", Icons.Rounded.ChatBubbleOutline),
    AI("AI behavior", "Global system prompt and response rules", Icons.Rounded.AutoAwesome),
    PROVIDERS("AI providers & models", "API keys and model routing", Icons.Rounded.Hub),
    SECURITY("Admin & security", "Admin access and local privacy", Icons.Rounded.Security),
    ABOUT("About", "Version and product information", Icons.Rounded.Info),
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onAdminLogin: () -> Unit = {}) {
    var selected by remember { mutableStateOf<SettingsCategory?>(null) }

    val settingsEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally(tween(300, easing = settingsEase)) { it } + fadeIn(tween(300, easing = settingsEase)) togetherWith
                    slideOutHorizontally(tween(230, easing = settingsEase)) { -it / 4 } + fadeOut(tween(230, easing = settingsEase))
            } else {
                slideInHorizontally(tween(300, easing = settingsEase)) { -it / 4 } + fadeIn(tween(300, easing = settingsEase)) togetherWith
                    slideOutHorizontally(tween(230, easing = settingsEase)) { it } + fadeOut(tween(230, easing = settingsEase))
            }
        },
        label = "settings-category-navigation",
    ) { category ->
        if (category == null) {
            SettingsCategoryHome(onBack = onBack) { selected = it }
        } else {
            SettingsCategoryDetail(category = category, onBack = { selected = null }, onAdminLogin = onAdminLogin)
        }
    }
}

@Composable
private fun SettingsCategoryHome(onBack: () -> Unit, onOpen: (SettingsCategory) -> Unit) {
    NeoPage("Settings", onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = NeoSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
        ) {
            item {
                Spacer(Modifier.height(NeoSpacing.sm))
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Everything is organized into focused sections so you can change one part of Neo GPT without hunting through a long page.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(NeoSpacing.lg))
            }
            items(SettingsCategory.values().size) { index ->
                val category = SettingsCategory.values()[index]
                NeoFeatureCard(
                    icon = category.icon,
                    title = category.title,
                    description = category.description,
                    onClick = { onOpen(category) },
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryDetail(
    category: SettingsCategory,
    onBack: () -> Unit,
    onAdminLogin: () -> Unit,
) {
    val context = LocalContext.current
    val storage = remember { SecureStorage(context) }
    val settings = remember { AppSettings.get(context) }
    val ui by settings.state.collectAsState()
    val systemPromptStore = remember { SystemPromptStore(context) }
    val adminAuth = remember { AdminAuth(context) }
    var adminLoggedIn by remember { mutableStateOf(adminAuth.isLoggedIn()) }

    NeoPage(category.title, onBack) {
        when (category) {
            SettingsCategory.APPEARANCE -> AppearanceSettings(settings, ui)
            SettingsCategory.CHAT -> ChatSettings(settings, ui)
            SettingsCategory.AI -> AiBehaviorSettings(systemPromptStore)
            SettingsCategory.PROVIDERS -> ProviderSettings(storage)
            SettingsCategory.SECURITY -> SecuritySettings(adminAuth, adminLoggedIn, onAdminLogin) { adminLoggedIn = it }
            SettingsCategory.ABOUT -> AboutSettings()
        }
    }
}

@Composable
private fun AppearanceSettings(settings: AppSettings, ui: AppSettings.State) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("Appearance", "Standard Material 3 surfaces are used throughout the app. Liquid Glass has been removed.")
            SettingSwitchCard(Icons.Rounded.AutoAwesomeMotion, "Smooth animations", "Keep Neo GPT's motion and interaction animations enabled.", ui.animations) { settings.setAnimations(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            SettingSwitchCard(Icons.Rounded.Palette, "Dynamic Material colors", "Use Android dynamic colors when available.", ui.dynamicColor) { settings.setDynamicColor(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            ThemeModeCard(ui.themeMode) { settings.setThemeMode(it) }
        }
    }
}

@Composable
private fun ChatSettings(settings: AppSettings, ui: AppSettings.State) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("Chat", "Tune the composer and AI response experience.")
            SettingSwitchCard(Icons.Rounded.VerticalAlignBottom, "Auto-scroll AI responses", "Keep the newest streamed content in view unless you scroll upward.", ui.autoScroll) { settings.setAutoScroll(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            SettingSwitchCard(Icons.Rounded.Send, "Enter to send", "Use the keyboard Send action to submit the current message.", ui.enterToSend) { settings.setEnterToSend(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            SettingSwitchCard(Icons.Rounded.Vibration, "Haptic feedback", "Use subtle haptics for supported primary interactions.", ui.haptics) { settings.setHaptics(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            SettingSwitchCard(Icons.Rounded.Schedule, "Show message timestamps", "Display message time metadata where supported.", ui.showTimestamps) { settings.setShowTimestamps(it) }
            Spacer(Modifier.height(NeoSpacing.sm))
            ResponseTextSizeCard(ui.responseTextScale) { settings.setResponseTextScale(it) }
        }
    }
}

@Composable
private fun AiBehaviorSettings(store: SystemPromptStore) {
    var systemPrompt by remember { mutableStateOf(store.get()) }
    var saved by remember { mutableStateOf(systemPrompt.isNotBlank()) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("AI behavior", "One global instruction is sent to every text-capable AI route used by Neo GPT.")
            ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
                Column(Modifier.padding(NeoSpacing.lg)) {
                    Text("Global system prompt", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(NeoSpacing.xs))
                    Text(
                        "Write the behavior, formatting rules and response style you want the connected models to follow. Provider safety policies and higher-priority instructions can still override it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(NeoSpacing.md))
                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { systemPrompt = it; saved = false },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 7,
                        maxLines = 16,
                        label = { Text("System prompt") },
                        placeholder = { Text("Example: Always answer clearly, use Markdown, and follow these rules…") },
                        shape = NeoShapes.large,
                    )
                    Spacer(Modifier.height(NeoSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                        Button(
                            onClick = { store.save(systemPrompt); systemPrompt = store.get(); saved = true },
                            enabled = systemPrompt.isNotBlank(),
                            shape = NeoShapes.pill,
                        ) {
                            Icon(Icons.Rounded.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (saved) "Saved" else "Save prompt")
                        }
                        if (systemPrompt.isNotBlank()) {
                            OutlinedButton(onClick = { store.clear(); systemPrompt = ""; saved = false }, shape = NeoShapes.pill) { Text("Clear") }
                        }
                    }
                    Spacer(Modifier.height(NeoSpacing.sm))
                    Text(
                        "Applied to Gemini, OpenRouter, NVIDIA NIM, Neo 4.1 Alpha and Gemini-powered Research where the provider supports system-level instructions.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderSettings(storage: SecureStorage) {
    var gemini by remember { mutableStateOf(storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()) }
    var openRouter by remember { mutableStateOf(storage.getProviderKey(AiProvider.OPENROUTER.id).orEmpty()) }
    var nvidia by remember { mutableStateOf(storage.getProviderKey(AiProvider.NVIDIA.id).orEmpty()) }
    var showGemini by remember { mutableStateOf(false) }
    var showOpenRouter by remember { mutableStateOf(false) }
    var showNvidia by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("AI providers", "Connect supported providers. API keys are kept in encrypted local storage.")
            ProviderSettingCard(AiProvider.GEMINI, gemini, { gemini = it }, showGemini, { showGemini = !showGemini }, saved == "gemini", { storage.clearProviderKey(AiProvider.GEMINI.id); gemini = ""; saved = null }) { storage.saveProviderKey(AiProvider.GEMINI.id, gemini); saved = "gemini" }
            Spacer(Modifier.height(NeoSpacing.sm))
            ProviderSettingCard(AiProvider.OPENROUTER, openRouter, { openRouter = it }, showOpenRouter, { showOpenRouter = !showOpenRouter }, saved == "openrouter", { storage.clearProviderKey(AiProvider.OPENROUTER.id); openRouter = ""; saved = null }) { storage.saveProviderKey(AiProvider.OPENROUTER.id, openRouter); saved = "openrouter" }
            Spacer(Modifier.height(NeoSpacing.sm))
            ProviderSettingCard(AiProvider.NVIDIA, nvidia, { nvidia = it }, showNvidia, { showNvidia = !showNvidia }, saved == "nvidia", { storage.clearProviderKey(AiProvider.NVIDIA.id); nvidia = ""; saved = null }) { storage.saveProviderKey(AiProvider.NVIDIA.id, nvidia); saved = "nvidia" }
            NeoSectionTitle("Model routing", "Configured providers appear in the model picker.")
            ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
                Column {
                    ListItem(headlineContent = { Text("Gemini") }, supportingContent = { Text("Native Gemini API models") }, leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) })
                    ListItem(headlineContent = { Text("OpenRouter") }, supportingContent = { Text("OpenAI-compatible streaming models") }, leadingContent = { Icon(Icons.Rounded.Hub, null) })
                    ListItem(headlineContent = { Text("NVIDIA NIM") }, supportingContent = { Text("NVIDIA chat-capable catalog models") }, leadingContent = { Icon(Icons.Rounded.Memory, null) })
                }
            }
        }
    }
}

@Composable
private fun SecuritySettings(adminAuth: AdminAuth, loggedIn: Boolean, onAdminLogin: () -> Unit, setLoggedIn: (Boolean) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("Admin & security", "Local access controls and privacy information.")
            if (loggedIn) {
                NeoFeatureCard(Icons.Rounded.AdminPanelSettings, "Admin mode enabled", "Neo 4.1 Alpha is available in the model picker.", {
                    adminAuth.logout(); setLoggedIn(false)
                })
            } else {
                NeoFeatureCard(Icons.Rounded.Lock, "Admin login", "Login locally to unlock Neo 4.1 Alpha. No Firebase or backend is used.", onAdminLogin)
            }
            Spacer(Modifier.height(NeoSpacing.sm))
            NeoFeatureCard(Icons.Rounded.Security, "Local API key protection", "Provider keys remain in encrypted Android Keystore-backed storage.", {})
        }
    }
}

@Composable
private fun AboutSettings() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl)) {
        item {
            NeoSectionTitle("About", "Neo GPT product information.")
            NeoFeatureCard(Icons.Rounded.AutoAwesome, "Neo GPT", "Version 1.8.0 • Material 3 • Multi-provider AI", {})
            Spacer(Modifier.height(NeoSpacing.sm))
            NeoFeatureCard(Icons.Rounded.DesignServices, "Design system", "Clean Material 3 surfaces, responsive motion and premium chat interactions.", {})
        }
    }
}

@Composable
private fun ResponseTextSizeCard(scale: Float, onChange: (Float) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Response text size", style = MaterialTheme.typography.titleMedium)
                Text("${(scale * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(NeoSpacing.xs))
            Text("Adjust how large AI answers appear in chat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(value = scale, onValueChange = onChange, valueRange = 0.80f..1.40f, steps = 7)
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
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Row(horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
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
