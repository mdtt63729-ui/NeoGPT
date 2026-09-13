package com.neogpt.app.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neogpt.app.NeoGptApplication
import com.neogpt.app.data.local.entity.ChatEntity
import com.neogpt.app.ui.components.NeoPage
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun ChatHistoryScreen(onBack: () -> Unit, onOpenChat: (chatId: String, modelId: String) -> Unit) {
    val context = LocalContext.current
    val database = (context.applicationContext as NeoGptApplication).database
    val chats by database.chatDao().getAllChats().collectAsState(initial = emptyList())
    NeoPage("Chat history", onBack) {
        if (chats.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Rounded.ChatBubbleOutline, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text("No conversations yet", style = MaterialTheme.typography.titleMedium)
                Text("Your conversations will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = NeoSpacing.xxl), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                item { Text("All conversations", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp)) }
                items(chats, key = { it.id }) { chat -> HistoryRow(chat) { onOpenChat(chat.id, chat.modelId) } }
            }
        }
    }
}

@Composable
private fun HistoryRow(chat: ChatEntity, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = NeoShapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ChatBubbleOutline, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(chat.title.ifBlank { "New Chat" }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(ChatViewModel.modelDisplayName(chat.modelId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
