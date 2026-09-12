package com.neogpt.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun ChatScreen(
    chatId: String,
    modelId: String,
    initialPrompt: String = "",
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = remember(modelId) { ChatViewModel(context, modelId) }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showMode by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val recordPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) composerText = composerText
    }

    LaunchedEffect(initialPrompt) {
        if (initialPrompt.isNotBlank() && state.messages.isEmpty()) {
            viewModel.sendMessage(initialPrompt)
        }
    }
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }
    LaunchedEffect(state.error) { showError = state.error != null }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoTopBar(
                title = state.modelName,
                onMenuClick = onOpenDrawer,
                showBack = true,
                onBackClick = onBack,
                onTitleClick = { },
                showTitleSelector = false,
                actionIcon = Icons.Rounded.MoreVert,
                onActionClick = { showMode = !showMode },
            )
        },
        bottomBar = {
            Column(Modifier.imePadding().navigationBarsPadding()) {
                if (showMode) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ComposerMode.values().filter { it != ComposerMode.DEFAULT }.forEach { mode ->
                            FilterChip(
                                selected = state.activeMode == mode,
                                onClick = { /* mode selection can be wired to tool execution */ },
                                label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                }
                NeoComposer(
                    text = composerText,
                    onTextChange = { composerText = it },
                    onSend = { viewModel.sendMessage(composerText); composerText = "" },
                    onAddClick = { showMode = !showMode },
                    onVoiceClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            // Android speech UI is handled by the platform; text entry remains available.
                        } else recordPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    isGenerating = state.isGenerating,
                    onStop = viewModel::stopGeneration,
                    activeMode = state.activeMode,
                    modifier = Modifier.padding(bottom = NeoSpacing.sm),
                )
            }
        },
    ) { padding ->
        if (state.messages.isEmpty()) {
            NeoEmptyState(
                icon = Icons.Rounded.AutoAwesome,
                title = "Start a conversation",
                description = "Ask a question, brainstorm an idea, or paste code to get started.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                state = listState,
                contentPadding = PaddingValues(vertical = NeoSpacing.md),
            ) {
                items(state.messages, key = { it.id }) { message ->
                    NeoMessage(
                        message = message,
                        onCopy = { viewModel.copyMessage(message.id) },
                        onRegenerate = { viewModel.regenerateMessage(message.id) },
                        onShare = { viewModel.shareMessage(message.id) },
                        onEdit = { viewModel.editMessage(message.id) },
                        onLike = { viewModel.likeMessage(message.id) },
                        onDislike = { viewModel.dislikeMessage(message.id) },
                    )
                }
            }
        }
    }

    if (showError && state.error != null) {
        AlertDialog(
            onDismissRequest = { showError = false },
            icon = { Icon(Icons.Rounded.ErrorOutline, null) },
            title = { Text("Neo GPT couldn't respond") },
            text = { Text(state.error ?: "") },
            confirmButton = { TextButton(onClick = { showError = false }) { Text("OK") } },
        )
    }
}
