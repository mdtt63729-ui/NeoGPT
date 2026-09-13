package com.neogpt.app.ui.screens.vibeee

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.NeoFeatureCard
import com.neogpt.app.ui.components.NeoPage
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun VibeeeScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Files", "Tasks", "Changes", "Terminal")
    NeoPage("Vibeee", onBack, Icons.Rounded.Code, {}) {
        Text("Agent workspace", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("A CodeX-style workspace for building, reviewing and running software with AI.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(NeoSpacing.md))
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 0.dp) {
            tabs.forEachIndexed { index, title -> Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) }) }
        }
        Spacer(Modifier.height(NeoSpacing.md))
        when (tab) {
            0 -> Overview()
            1 -> WorkspaceCard("Files", listOf("app/src", "gradle", "README.md", "build.gradle.kts"))
            2 -> WorkspaceCard("Agent tasks", listOf("Inspect repository", "Implement change", "Run checks", "Review diff"))
            3 -> WorkspaceCard("Changes", listOf("Working tree", "Patch preview", "Review status", "Checkpoint"))
            else -> TerminalCard()
        }
    }
}

@Composable private fun Overview() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        NeoFeatureCard(Icons.Rounded.AutoAwesome, "AI coding agent", "Plan work, inspect files, modify code and review the result.", {})
        NeoFeatureCard(Icons.Rounded.AccountTree, "Plan & tasks", "Break a request into focused tasks with checkpoints and approvals.", {})
        NeoFeatureCard(Icons.Rounded.Difference, "Diff review", "Inspect changes before they are accepted into the working tree.", {})
        NeoFeatureCard(Icons.Rounded.BugReport, "Tests & checks", "Run checks and keep failures visible in one focused workspace.", {})
        NeoFeatureCard(Icons.Rounded.Terminal, "Terminal", "A dedicated execution surface prepared for a future runtime integration.", {})
        NeoFeatureCard(Icons.Rounded.CloudUpload, "Git workflow", "Branch, commit and pull-request surfaces are prepared for later integration.", {})
    }
}

@Composable private fun WorkspaceCard(title: String, rows: List<String>) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column(Modifier.padding(vertical = 6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            rows.forEach { row -> ListItem(headlineContent = { Text(row) }, leadingContent = { Icon(Icons.Rounded.ChevronRight, null) }) }
        }
    }
}

@Composable private fun TerminalCard() {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Text("Terminal", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("$ vibeee agent ready", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("Execution UI is ready for the next integration phase.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
