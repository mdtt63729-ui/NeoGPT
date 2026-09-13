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
    val icon = when (type) {
        AttachmentType.IMAGE -> Icons.Rounded.Image
        AttachmentType.FILE -> Icons.Rounded.Description
        AttachmentType.AUDIO -> Icons.Rounded.AudioFile
        AttachmentType.VIDEO -> Icons.Rounded.VideoFile
    }
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
fun NeoAttachmentCard(
    name: String,
    mimeType: String,
    sizeBytes: Long,
    localUri: String? = null,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon = when {
        mimeType == "application/pdf" -> Icons.Rounded.PictureAsPdf
        mimeType.contains("zip") || name.endsWith(".zip", true) -> Icons.Rounded.Archive
        mimeType.startsWith("image/") -> Icons.Rounded.Image
        mimeType.startsWith("audio/") -> Icons.Rounded.AudioFile
        mimeType.startsWith("video/") -> Icons.Rounded.VideoFile
        else -> Icons.Rounded.Description
    }
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .48f)),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (mimeType.startsWith("image/") && !localUri.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = android.net.Uri.parse(localUri),
                    contentDescription = name,
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(13.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            } else {
                Surface(Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(
                    buildString {
                        append(formatAttachmentSize(sizeBytes))
                        if (mimeType.isNotBlank() && mimeType != "application/octet-stream") append(" • ").append(mimeType.substringAfter('/').uppercase())
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Icon(Icons.Rounded.ChevronRight, "Open file", Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatAttachmentSize(bytes: Long): String = when {
    bytes <= 0L -> "Unknown size"
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}
