package com.neogpt.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoFileChip(name: String, type: AttachmentType, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val icon = when (type) { AttachmentType.IMAGE -> Icons.Rounded.Image; AttachmentType.FILE -> Icons.Rounded.Description; AttachmentType.AUDIO -> Icons.Rounded.AudioFile; AttachmentType.VIDEO -> Icons.Rounded.VideoFile }
    Surface(modifier.animateContentSize(), shape = NeoShapes.medium, color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 1.dp) {
        Row(Modifier.padding(start = NeoSpacing.sm, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(7.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) { Icon(Icons.Rounded.Clear, "Remove attachment", Modifier.size(15.dp)) }
        }
    }
}

@Composable
fun NeoAttachmentCard(name: String, mimeType: String, sizeBytes: Long, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val icon = when { mimeType == "application/pdf" -> Icons.Rounded.PictureAsPdf; mimeType.startsWith("image/") -> Icons.Rounded.Image; mimeType.startsWith("audio/") -> Icons.Rounded.AudioFile; mimeType.startsWith("video/") -> Icons.Rounded.VideoFile; else -> Icons.Rounded.Description }
    Surface(onClick = onOpen, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), color = MaterialTheme.colorScheme.surfaceContainer, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .58f))) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) } }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatAttachmentSize(sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Open", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun formatAttachmentSize(bytes: Long): String = when {
    bytes <= 0L -> "File"
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}
