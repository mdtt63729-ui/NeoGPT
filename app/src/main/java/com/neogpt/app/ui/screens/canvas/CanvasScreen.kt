package com.neogpt.app.ui.screens.canvas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun CanvasScreen(onBack: () -> Unit) {
    var text by remember { mutableStateOf("") }
    NeoPage("Canvas", onBack, Icons.Rounded.Save, {}) {
        NeoSectionTitle("Infinite canvas", "Draft notes, plans and structured ideas alongside your AI work.")
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            placeholder = { Text("Start writing…") },
            shape = com.neogpt.app.ui.theme.NeoShapes.large,
        )
        Spacer(Modifier.height(NeoSpacing.md))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
            AssistChip(onClick = { text += if (text.isEmpty()) "Idea: " else "\\nIdea: " }, label = { Text("Add idea") }, leadingIcon = { Icon(Icons.Rounded.Lightbulb, null) })
            AssistChip(onClick = { text += if (text.isEmpty()) "Task: " else "\\nTask: " }, label = { Text("Add task") }, leadingIcon = { Icon(Icons.Rounded.CheckCircleOutline, null) })
        }
        Spacer(Modifier.height(NeoSpacing.md))
    }
}
