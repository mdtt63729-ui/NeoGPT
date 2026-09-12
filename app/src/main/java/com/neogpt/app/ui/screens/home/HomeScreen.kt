package com.neogpt.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.ui.components.NeoComposer
import com.neogpt.app.ui.components.NeoModelPickerSheet
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenChat: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = remember { HomeViewModel(context) }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .statusBarsPadding()
                    .padding(top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Menu, "Open menu", Modifier.size(25.dp))
                    }
                }
                Surface(
                    onClick = { showModelPicker = true },
                    shape = NeoShapes.pill,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(state.selectedModel.name, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.size(3.dp))
                        Icon(Icons.Rounded.KeyboardArrowDown, null, Modifier.size(18.dp))
                    }
                }
                Surface(
                    onClick = { onOpenChat(state.selectedModel.id, "") },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AddComment, "New chat", Modifier.size(23.dp))
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = NeoSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "Neo GPT",
                style = MaterialTheme.typography.displayMedium,
                fontFamily = NeoFontFamily,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1.35f))
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
                modifier = Modifier.padding(bottom = NeoSpacing.sm),
            )
            Text(
                "AI can make mistakes. Check important information.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = NeoSpacing.sm),
            )
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
