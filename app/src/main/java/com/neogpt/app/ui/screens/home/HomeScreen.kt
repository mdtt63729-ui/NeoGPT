package com.neogpt.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenChat: (String, String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenCustomAI: () -> Unit,
) {
    val viewModel: HomeViewModel = remember { HomeViewModel() }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoTopBar(
                title = state.selectedModel.name,
                onMenuClick = onOpenDrawer,
                onTitleClick = { showModelPicker = true },
                showTitleSelector = true,
                actionIcon = Icons.Rounded.Search,
                onActionClick = onOpenSearch,
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = NeoSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Surface(
                modifier = Modifier.size(76.dp),
                shape = NeoShapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.height(NeoSpacing.lg))
            Text("Neo GPT", fontFamily = NeoFontFamily, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(NeoSpacing.sm))
            Text(state.greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(NeoSpacing.xxl))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                QuickAction(Icons.Rounded.Search, "Search", onOpenSearch, Modifier.weight(1f))
                QuickAction(Icons.Rounded.Folder, "Projects", onOpenProjects, Modifier.weight(1f))
                QuickAction(Icons.Rounded.AutoAwesome, "Custom AI", onOpenCustomAI, Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            NeoComposer(
                text = composerText,
                onTextChange = { composerText = it },
                onSend = {
                    if (composerText.isNotBlank()) {
                        val text = composerText.trim()
                        composerText = ""
                        onOpenChat(state.selectedModel.id, text)
                    }
                },
                onAddClick = { },
                onVoiceClick = { },
                placeholder = "Ask anything…",
                modifier = Modifier.navigationBarsPadding(),
            )
            Spacer(Modifier.height(NeoSpacing.md))
            Text("AI can make mistakes. Check important information.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(NeoSpacing.sm))
        }
    }

    if (showModelPicker) {
        NeoModelPickerSheet(
            models = state.availableModels,
            selectedModelId = state.selectedModel.id,
            onSelect = { viewModel.selectModel(it); showModelPicker = false },
            onDismiss = { showModelPicker = false },
        )
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier.height(52.dp), shape = NeoShapes.large) {
        Icon(icon, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label)
    }
}
