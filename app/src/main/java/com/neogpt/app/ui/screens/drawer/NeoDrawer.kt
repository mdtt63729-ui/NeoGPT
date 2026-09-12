package com.neogpt.app.ui.screens.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.neogpt.app.R
import com.neogpt.app.ui.navigation.NeoRoutes
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.foundation.layout.fillMaxWidth
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun NeoDrawer(
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
) {
    val primaryItems = listOf(
        DrawerItem("New Chat", Icons.Rounded.AddComment, NeoRoutes.HOME),
        DrawerItem("Chats", Icons.Rounded.ChatBubbleOutline, NeoRoutes.HOME),
    )
    val workspaceItems = listOf(
        DrawerItem("Projects", Icons.Rounded.Folder, NeoRoutes.PROJECTS),
        DrawerItem("Files", Icons.Rounded.Description, NeoRoutes.FILES),
        DrawerItem("Canvas", Icons.Rounded.Draw, NeoRoutes.CANVAS),
        DrawerItem("Research", Icons.Rounded.TravelExplore, NeoRoutes.RESEARCH),
        DrawerItem("Code", Icons.Rounded.Code, NeoRoutes.CODE),
        DrawerItem("Tasks", Icons.Rounded.TaskAlt, NeoRoutes.TASKS),
    )
    val aiItems = listOf(
        DrawerItem("Custom AI", Icons.Rounded.AutoAwesome, NeoRoutes.CUSTOM_AI),
        DrawerItem("Models", Icons.Rounded.AutoAwesome, NeoRoutes.SETTINGS),
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = MaterialTheme.shapes.large,
        modifier = Modifier.width(NeoDimens.drawerWidth),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(NeoSpacing.lg),
        ) {
            // Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.neo_app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp).clip(CircleShape),
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Neo GPT",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = NeoFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "AI workspace",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(NeoSpacing.xxl))

            DrawerSection(label = "PRIMARY", items = primaryItems, onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(NeoSpacing.lg))

            DrawerSection(label = "WORKSPACE", items = workspaceItems, onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(NeoSpacing.lg))

            DrawerSection(label = "AI", items = aiItems, onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(NeoSpacing.md))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            ) {
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { onNavigate(NeoRoutes.SETTINGS) },
                    icon = { androidx.compose.material3.Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                    colors = NavigationDrawerItemDefaults.colors(),
                )
                NavigationDrawerItem(
                    label = { Text("Help") },
                    selected = false,
                    onClick = { onNavigate(NeoRoutes.HOME) },
                    icon = { androidx.compose.material3.Icon(Icons.Rounded.Help, contentDescription = "Help") },
                    colors = NavigationDrawerItemDefaults.colors(),
                )
            }
        }
    }
}

@Composable
private fun DrawerSection(
    label: String,
    items: List<DrawerItem>,
    onNavigate: (String) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = NeoSpacing.md, bottom = NeoSpacing.sm),
    )
    items.forEach { item ->
        NavigationDrawerItem(
            label = { Text(item.label, style = MaterialTheme.typography.bodyMedium) },
            selected = false,
            onClick = { onNavigate(item.route) },
            icon = { androidx.compose.material3.Icon(item.icon, contentDescription = item.label) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = MaterialTheme.colorScheme.surface,
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
    }
}
