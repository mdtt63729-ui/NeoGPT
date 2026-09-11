package com.neogpt.app.ui.screens.code

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoCodeStyle
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun CodeScreen(onBack: () -> Unit) {
    var code by remember { mutableStateOf("// Write or paste code here\\nfun main() {\\n    println(\"Hello Neo GPT\")\\n}") }
    var output by remember { mutableStateOf("Ready. Use Run to validate the editor flow.") }
    NeoPage("Code", onBack, Icons.Rounded.PlayArrow, { output = "Code check completed locally. Connect a runtime tool to execute language-specific code." }) {
        NeoSectionTitle("Code workspace", "A focused editor for drafting, reviewing and preparing code.")
        Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxSize().padding(NeoSpacing.lg)) {
                BasicTextField(
                    value = code,
                    onValueChange = { code = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = NeoCodeStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )
            }
        }
        Spacer(Modifier.height(NeoSpacing.md))
        ElevatedCard(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Output") },
                supportingContent = { Text(output) },
                leadingContent = { Icon(Icons.Rounded.Terminal, null) },
            )
        }
        Spacer(Modifier.height(NeoSpacing.md))
    }
}
