package com.neogpt.app.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun ProjectDetailScreen(projectId: String, onBack: () -> Unit) {
    var name by remember { mutableStateOf(if (projectId == "new") "" else "Project ${projectId}") }
    var description by remember { mutableStateOf("") }
    NeoPage("Project", onBack, Icons.Rounded.Save, {}) {
        NeoSectionTitle("Project settings", "Keep the context that should travel with this workspace.")
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Project name") }, singleLine = true)
        Spacer(Modifier.height(NeoSpacing.md))
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth().heightIn(min = 140.dp), label = { Text("Description") })
        Spacer(Modifier.height(NeoSpacing.lg))
        NeoFeatureCard(Icons.Rounded.ChatBubbleOutline, "Project chats", "Open conversations associated with this project.", {})
        Spacer(Modifier.height(NeoSpacing.sm))
        NeoFeatureCard(Icons.Rounded.FolderOpen, "Project files", "Keep source material available to the assistant.", {})
        Spacer(Modifier.height(NeoSpacing.lg))
        Button(onClick = onBack, Modifier.fillMaxWidth(), shape = NeoShapes.pill) { Text("Save project") }
    }
}
