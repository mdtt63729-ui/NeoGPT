package com.neogpt.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun TasksScreen(onBack: () -> Unit) {
    data class UiTask(val name: String, val schedule: String, val enabled: Boolean)
    val tasks = remember { mutableStateListOf(UiTask("Daily briefing", "Every morning", true), UiTask("Weekly review", "Every Sunday", false)) }
    var showAdd by remember { mutableStateOf(false) }
    NeoPage("Tasks", onBack, Icons.Rounded.Add, { showAdd = true }) {
        NeoSectionTitle("Automations", "Keep repeatable work ready to run.")
        tasks.forEachIndexed { index, task ->
            ElevatedCard(Modifier.fillMaxWidth().padding(bottom = NeoSpacing.sm)) {
                ListItem(
                    headlineContent = { Text(task.name) },
                    supportingContent = { Text(task.schedule) },
                    leadingContent = { Icon(Icons.Rounded.Schedule, null) },
                    trailingContent = {
                        Switch(checked = task.enabled, onCheckedChange = { tasks[index] = task.copy(enabled = it) })
                    },
                )
            }
        }
        if (showAdd) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAdd = false },
                title = { Text("Create task") },
                text = {
                    OutlinedTextField(name, { name = it }, label = { Text("Task name") }, singleLine = true)
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotBlank()) tasks.add(UiTask(name.trim(), "Manual", true))
                        showAdd = false
                    }) { Text("Create") }
                },
                dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
            )
        }
    }
}
