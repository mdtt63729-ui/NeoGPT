package com.neogpt.app.ui.screens.files

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun FilesScreen(onBack: () -> Unit) {
    val files = remember { mutableStateListOf<String>() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { files.add(it.lastPathSegment ?: "Selected file") }
    }
    NeoPage("Files", onBack, actionIcon = Icons.Rounded.Add, onAction = {
        picker.launch(arrayOf("*/*"))
    }) {
        NeoSectionTitle("Your files", "${files.size} files available to attach to a chat")
        if (files.isEmpty()) {
            NeoEmptyState(
                Icons.Rounded.FolderOpen,
                "No files yet",
                "Import documents, images or other files and keep them ready for your conversations.",
                "Add files",
            ) { picker.launch(arrayOf("*/*")) }
        } else {
            files.forEach { name ->
                ElevatedCard(Modifier.fillMaxWidth().padding(bottom = NeoSpacing.sm)) {
                    ListItem(
                        headlineContent = { Text(name) },
                        leadingContent = { Icon(Icons.Rounded.InsertDriveFile, null) },
                        trailingContent = { IconButton(onClick = { files.remove(name) }) { Icon(Icons.Rounded.DeleteOutline, "Delete") } },
                    )
                }
            }
        }
    }
}
