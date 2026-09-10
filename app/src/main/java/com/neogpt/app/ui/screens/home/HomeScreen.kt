package com.neogpt.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.NeoComposer
import com.neogpt.app.ui.components.NeoModelPickerSheet
import com.neogpt.app.ui.components.NeoModelInfo
import com.neogpt.app.ui.components.NeoTopBar
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenCustomAI: () -> Unit,
) {
    val viewModel: HomeViewModel = remember { HomeViewModel() }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top bar
            NeoTopBar(
                title = state.selectedModel.name,
                onMenuClick = onOpenDrawer,
                onTitleClick = { showModelPicker = true },
                onActionClick = onOpenSettings,
            )

            // Center: greeting + brand
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = NeoSpacing.xxl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Subtle brand glow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(0.05f)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = androidx.compose.foundation.shape.CircleShape,
                        )
                )

                Spacer(modifier = Modifier.height(NeoSpacing.lg))

                // Brand name
                Text(
                    text = "Neo GPT",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = NeoFontFamily,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(NeoSpacing.sm))

                // Dynamic greeting
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Composer
            NeoComposer(
                text = composerText,
                onTextChange = { composerText = it },
                onSend = {
                    if (composerText.isNotBlank()) {
                        viewModel.sendMessage(composerText)
                        composerText = ""
                        onOpenChat()
                    }
                },
                onAddClick = { /* open add menu bottom sheet */ },
                onVoiceClick = { /* open voice mode */ },
                modifier = Modifier.navigationBarsPadding(),
                placeholder = "Ask anything…",
            )

            Spacer(modifier = Modifier.height(NeoSpacing.lg))
        }
    }

    // Model Picker Bottom Sheet
    if (showModelPicker) {
        NeoModelPickerSheet(
            models = state.availableModels,
            selectedModelId = state.selectedModel.id,
            onSelect = { model ->
                viewModel.selectModel(model)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false },
        )
    }
}
