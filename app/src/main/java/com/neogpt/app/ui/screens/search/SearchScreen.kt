package com.neogpt.app.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.NeoPage
import com.neogpt.app.ui.components.NeoEmptyState
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun SearchScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf("Latest AI models", "Project ideas", "Kotlin Compose") }
    val results = remember(query) {
        if (query.isBlank()) emptyList() else history.filter { it.contains(query, true) }
    }
    NeoPage("Search", onBack) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            placeholder = { Text("Search chats, projects and files") },
            shape = NeoShapes.xlarge,
        )
        Spacer(Modifier.height(NeoSpacing.md))
        if (query.isBlank()) {
            Text("Recent searches", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(NeoSpacing.sm))
            history.forEach { item ->
                ListItem(
                    headlineContent = { Text(item) },
                    leadingContent = { Icon(Icons.Rounded.History, null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                )
            }
        } else if (results.isEmpty()) {
            NeoEmptyState(Icons.Rounded.Search, "No matches", "Try a different search phrase.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { item -> ListItem(headlineContent = { Text(item) }) }
            }
        }
    }
}
