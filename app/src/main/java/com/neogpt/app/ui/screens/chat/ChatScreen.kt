package com.neogpt.app.ui.screens.chat

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.ComposerMode
import com.neogpt.app.ui.components.NeoComposer
import com.neogpt.app.ui.components.NeoMessage
import com.neogpt.app.ui.components.NeoMessageData
import com.neogpt.app.ui.components.NeoTopBar
import com.neogpt.app.ui.components.MessageRole
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun ChatScreen(
    chatId: String,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val viewModel: ChatViewModel = remember { ChatViewModel() }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

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
                title = state.modelName,
                onMenuClick = onOpenDrawer,
                onTitleClick = { /* open model picker */ },
                showBack = true,
                onBackClick = onBack,
            )

            // Messages
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = NeoSpacing.md),
            ) {
                items(state.messages) { message ->
                    NeoMessage(
                        message = message,
                        onCopy = { viewModel.copyMessage(message.id) },
                        onRegenerate = { viewModel.regenerateMessage(message.id) },
                        onShare = { viewModel.shareMessage(message.id) },
                        onEdit = { viewModel.editMessage(message.id) },
                        onLike = { viewModel.likeMessage(message.id) },
                        onDislike = { viewModel.dislikeMessage(message.id) },
                        onMore = { /* show more options */ },
                    )
                }
            }

            // Composer
            NeoComposer(
                text = composerText,
                onTextChange = { composerText = it },
                onSend = {
                    if (composerText.isNotBlank()) {
                        viewModel.sendMessage(composerText)
                        composerText = ""
                    }
                },
                onAddClick = { /* open add menu */ },
                onVoiceClick = { /* open voice mode */ },
                isGenerating = state.isGenerating,
                onStop = { viewModel.stopGeneration() },
                activeMode = state.activeMode,
                modifier = Modifier.navigationBarsPadding(),
            )

            Spacer(modifier = Modifier.height(NeoSpacing.lg))
        }
    }
}
