package com.neogpt.app.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun ProjectsScreen(onBack: () -> Unit, onProjectClick: (String) -> Unit, onNewProject: () -> Unit) {
    val projects = remember { mutableStateListOf("MIRA Assistant", "Neo GPT Mobile") }
    var showAdd by remember { mutableStateOf(false) }
    NeoPage("Projects", onBack, Icons.Rounded.Add, { showAdd = true }) {
        NeoSectionTitle("Workspace", "${projects.size} active projects")
        if (projects.isEmpty()) NeoEmptyState(Icons.Rounded.Folder, "No projects", "Create a project to keep chats, files and instructions together.", "Create project") { showAdd = true }
        projects.forEachIndexed { index, name ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = NeoSpacing.sm),
                onClick = { onProjectClick(index.toString()) },
            ) {
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = { Text("Chats, files and instructions") },
                    leadingContent = { Icon(Icons.Rounded.Folder, null) },
                    trailingContent = { Icon(Icons.Rounded.ChevronRight, null) },
                )
            }
        }
        if (showAdd) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAdd = false },
                title = { Text("New project") },
                text = { OutlinedTextField(name, { name = it }, label = { Text("Project name") }, singleLine = true) },
                confirmButton = { TextButton(onClick = { if (name.isNotBlank()) projects.add(name.trim()); showAdd = false }) { Text("Create") } },
                dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
            )
        }
    }
}
