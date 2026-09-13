package com.neogpt.app.ui.screens.vibeee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.NeoPage
import com.neogpt.app.ui.components.NeoSectionTitle
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

/** CodeX-style workspace shell. The controls are intentionally UI-first until the runtime is connected. */
@Composable
fun VibeeeScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Files", "Tasks", "Changes", "Terminal")

    NeoPage("Vibeee", onBack, Icons.Rounded.Code) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = NeoSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
        ) {
            item {
                Spacer(Modifier.height(NeoSpacing.sm))
                WorkspaceHeader()
                Spacer(Modifier.height(NeoSpacing.md))
                PrimaryTabRow(selectedTabIndex = tab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                    }
                }
                Spacer(Modifier.height(NeoSpacing.md))
            }
            when (tab) {
                0 -> item { Overview() }
                1 -> item { FileWorkspace() }
                2 -> item { TaskWorkspace() }
                3 -> item { ChangeWorkspace() }
                4 -> item { TerminalWorkspace() }
            }
        }
    }
}

@Composable
private fun WorkspaceHeader() {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.xlarge) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(46.dp), CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Code, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Vibeee workspace", style = MaterialTheme.typography.titleLarge)
                    Text("AI coding workspace", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = {}, label = { Text("Ready") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, null, Modifier.size(16.dp)) })
            }
            Spacer(Modifier.height(NeoSpacing.md))
            Text("Plan, inspect, edit, test and review a project from one focused surface.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Overview() {
    Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
        VibeeeFeature(Icons.Rounded.AutoAwesome, "AI agent", "Plan a task, inspect the workspace and prepare implementation steps.")
        VibeeeFeature(Icons.Rounded.FolderOpen, "Workspace files", "Browse project files and keep relevant context close to the task.")
        VibeeeFeature(Icons.Rounded.AccountTree, "Plan & checkpoints", "Break work into reviewable steps with checkpoints and approvals.")
        VibeeeFeature(Icons.Rounded.Difference, "Diff review", "Preview proposed edits before accepting them into the working tree.")
        VibeeeFeature(Icons.Rounded.BugReport, "Tests & checks", "Keep build, test and static-check results visible beside the task.")
        VibeeeFeature(Icons.Rounded.Terminal, "Terminal", "A dedicated execution surface ready for a future runtime connection.")
        VibeeeFeature(Icons.Rounded.CloudUpload, "Git workflow", "Prepare branches, commits and pull requests for later integration.")
    }
}

@Composable
private fun VibeeeFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        ListItem(
            leadingContent = {
                Surface(Modifier.size(42.dp), NeoShapes.medium, MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            },
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(description, style = MaterialTheme.typography.bodySmall) },
            trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
        )
    }
}

@Composable
private fun FileWorkspace() {
    val files = listOf("app/src/main", "app/build.gradle.kts", "gradle/libs.versions.toml", "README.md")
    WorkspaceList("Files", files, Icons.Rounded.InsertDriveFile)
}

@Composable
private fun TaskWorkspace() {
    val tasks = listOf("Inspect repository", "Implement requested change", "Run build and checks", "Review final diff")
    WorkspaceList("Agent tasks", tasks, Icons.Rounded.CheckCircleOutline)
}

@Composable
private fun ChangeWorkspace() {
    Column(verticalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
        WorkspaceMetric("Working tree", "Ready for changes", Icons.Rounded.FolderOpen)
        WorkspaceMetric("Patch preview", "No runtime connected", Icons.Rounded.Difference)
        WorkspaceMetric("Checkpoint", "Safe restore point", Icons.Rounded.BookmarkBorder)
        WorkspaceMetric("Review", "Waiting for implementation", Icons.Rounded.RateReview)
    }
}

@Composable
private fun TerminalWorkspace() {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column(Modifier.padding(NeoSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Terminal, null)
                Spacer(Modifier.width(9.dp))
                Text("Terminal", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(NeoSpacing.md))
            Surface(Modifier.fillMaxWidth(), shape = NeoShapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text("$ vibeee agent ready\n$ waiting for runtime connection", Modifier.padding(NeoSpacing.md), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun WorkspaceList(title: String, rows: List<String>, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(NeoSpacing.lg))
            rows.forEach { row ->
                ListItem(headlineContent = { Text(row) }, leadingContent = { Icon(icon, null) }, trailingContent = { Icon(Icons.Rounded.ChevronRight, null) })
            }
        }
    }
}

@Composable
private fun WorkspaceMetric(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = NeoShapes.large) {
        ListItem(
            leadingContent = { Icon(icon, null) },
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(value, style = MaterialTheme.typography.bodySmall) },
        )
    }
}
