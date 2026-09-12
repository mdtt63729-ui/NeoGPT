package com.neogpt.app.ui.screens.customai

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
fun CustomAIScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("My Assistant") }
    var instructions by remember { mutableStateOf("You are a helpful, concise AI assistant.") }
    var saved by remember { mutableStateOf(false) }
    NeoPage("Custom AI", onBack, Icons.Rounded.Save, { saved = true }) {
        NeoSectionTitle("Build your assistant", "Give your assistant a name and a behavior that matches your workflow.")
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
        Spacer(Modifier.height(NeoSpacing.md))
        OutlinedTextField(instructions, { instructions = it }, Modifier.fillMaxWidth().heightIn(min = 160.dp), label = { Text("Instructions") })
        Spacer(Modifier.height(NeoSpacing.md))
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(if (saved) "Saved" else "Draft assistant") },
                supportingContent = { Text("Changes are kept for this session.") },
                leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
            )
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        Button(onClick = { saved = true }, Modifier.fillMaxWidth(), shape = NeoShapes.pill) { Text("Save assistant") }
    }
}
