package com.neogpt.app.ui.screens.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neogpt.app.NeoGptApplication
import com.neogpt.app.R
import com.neogpt.app.data.local.entity.ChatEntity
import com.neogpt.app.ui.navigation.NeoRoutes
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoShapes

@Composable
fun NeoDrawer(
    onNavigate: (String) -> Unit,
    onOpenChat: (chatId: String, modelId: String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val database = (context.applicationContext as NeoGptApplication).database
    val chats by database.chatDao().getAllChats().collectAsState(initial = emptyList())

    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface, drawerShape = NeoShapes.xlarge, modifier = Modifier.width(NeoDimens.drawerWidth)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.neo_app_icon), null, Modifier.size(42.dp).clip(CircleShape))
                Spacer(Modifier.width(12.dp))
                Text("Neo GPT", style = MaterialTheme.typography.titleLarge, fontFamily = NeoFontFamily, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { onNavigate(NeoRoutes.SEARCH) }) { Icon(Icons.Rounded.Search, "Search chats") }
            }

            Text("CHAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp))
            DrawerAction("New chat", Icons.Rounded.Edit, { onNavigate(NeoRoutes.HOME) })
            DrawerAction("Chat history", Icons.Rounded.ChatBubbleOutline, { onNavigate(NeoRoutes.CHAT_HISTORY) })

            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                item { Text("RECENTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) }
                if (chats.isEmpty()) {
                    item { Text("No chats yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(chats.take(14), key = { it.id }) { chat -> RecentChatRow(chat) { onOpenChat(chat.id, chat.modelId) } }
                }
                item {
                    Spacer(Modifier.height(10.dp))
                    Text("WORKSPACE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp))
                    DrawerAction("Images", Icons.Rounded.Image, { onNavigate(NeoRoutes.FILES) })
                    DrawerAction("Library", Icons.Rounded.LibraryBooks, { onNavigate(NeoRoutes.FILES) })
                    DrawerAction("Projects", Icons.Rounded.Folder, { onNavigate(NeoRoutes.PROJECTS) })
                    DrawerAction("Vibeee", Icons.Rounded.Code, { onNavigate(NeoRoutes.VIBEEE) })
                    DrawerAction("Canvas", Icons.Rounded.Draw, { onNavigate(NeoRoutes.CANVAS) })
                    DrawerAction("Research", Icons.Rounded.TravelExplore, { onNavigate(NeoRoutes.RESEARCH) })
                    DrawerAction("Code", Icons.Rounded.Terminal, { onNavigate(NeoRoutes.CODE) })
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))
            Surface(Modifier.fillMaxWidth().padding(12.dp), shape = NeoShapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("DM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) } }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dhun Music", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Account", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onNavigate(NeoRoutes.SETTINGS) }) { Icon(Icons.Rounded.Settings, "Settings") }
                }
            }
        }
    }
}

@Composable private fun DrawerAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(label = { Text(label, style = MaterialTheme.typography.bodyLarge) }, selected = false, onClick = onClick, icon = { Icon(icon, null) }, shape = NeoShapes.medium, modifier = Modifier.padding(horizontal = 8.dp), colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = MaterialTheme.colorScheme.surface))
}

@Composable private fun RecentChatRow(chat: ChatEntity, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = NeoShapes.medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.ChatBubbleOutline, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(10.dp))
            Text(chat.title.ifBlank { "New Chat" }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
