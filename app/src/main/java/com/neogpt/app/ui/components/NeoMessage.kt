package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.domain.model.Attachment

enum class MessageRole { USER, AI }

data class NeoMessageData(
    val id: String,
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val isImageGenerating: Boolean = false,
    val imageCreated: Boolean = false,
    val agentSteps: List<AgentStep> = emptyList(),
    val toolLabels: List<String> = emptyList(),
    val imageProgress: Int = 0,
    val imageStatusText: String = "Sketching it out…",
    val attachments: List<Attachment> = emptyList(),
)

@Composable
fun NeoMessage(
    message: NeoMessageData,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit = {},
    onRegenerate: () -> Unit = {},
    onShare: () -> Unit = {},
    onEdit: () -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onMore: () -> Unit = {},
    onDownloadImage: () -> Unit = {},
    onOpenAttachment: (Attachment) -> Unit = {},
    responseTextScale: Float = 1f,
) {
    var userActions by remember { mutableStateOf(false) }
    if (message.role == MessageRole.USER) {
        Column(
            modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .combinedClickable(onClick = { userActions = !userActions }, onLongClick = { userActions = true })
                    .animateContentSize(spring(dampingRatio = .94f, stiffness = 400f)),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (message.content.isNotBlank()) {
                        SelectionContainer {
                            Text(message.content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    message.attachments.forEach { attachment ->
                        if (message.content.isNotBlank()) Spacer(Modifier.height(9.dp))
                        NeoAttachmentCard(
                            name = attachment.name,
                            mimeType = attachment.mimeType,
                            sizeBytes = attachment.sizeBytes,
                            localUri = attachment.localUri,
                            onOpen = { onOpenAttachment(attachment) },
                        )
                    }
                }
            }
            AnimatedVisibility(userActions, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
                MessageToolbar(user = true, onCopy, onRegenerate, onShare, onEdit, onLike, onDislike, onMore)
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).animateContentSize(spring(dampingRatio = .95f, stiffness = 360f)),
        ) {
            if (message.agentSteps.isNotEmpty() && message.content.isBlank() && !message.isImageGenerating) {
                AgentStatusStack(message.agentSteps, toolLabels = message.toolLabels)
                Spacer(Modifier.height(10.dp))
            } else if (message.content.isEmpty() && message.isStreaming && !message.isImageGenerating) {
                NeoThinkingIndicator()
            }
            if (message.content.isNotEmpty()) {
                SelectionContainer {
                    NeoMarkdown(message.content, isStreaming = message.isStreaming, textScale = responseTextScale)
                }
            }
            if (message.attachments.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                message.attachments.forEach { attachment ->
                    NeoAttachmentCard(attachment.name, attachment.mimeType, attachment.sizeBytes, attachment.localUri, { onOpenAttachment(attachment) })
                    Spacer(Modifier.height(6.dp))
                }
            }
            if (message.imageUrl != null || message.isImageGenerating) {
                Spacer(Modifier.height(12.dp))
                NeoImageGenerationCard(message.imageUrl, message.isImageGenerating, message.imageCreated, message.imageProgress, message.imageStatusText, onDownloadImage)
            }
            if (message.isStreaming && message.content.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                StreamingCursor()
            }
            AnimatedVisibility(message.content.isNotBlank() && !message.isStreaming, enter = fadeIn(tween(180)), exit = fadeOut(tween(100))) {
                MessageToolbar(user = false, onCopy, onRegenerate, onShare, onEdit, onLike, onDislike, onMore)
            }
        }
    }
}

@Composable
private fun MessageToolbar(user: Boolean, onCopy: () -> Unit, onRegenerate: () -> Unit, onShare: () -> Unit, onEdit: () -> Unit, onLike: () -> Unit, onDislike: () -> Unit, onMore: () -> Unit) {
    Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.CenterVertically) {
        NeoToolbarIcon(Icons.Rounded.ContentCopy, "Copy", onCopy)
        if (user) {
            NeoToolbarIcon(Icons.Rounded.Edit, "Edit", onEdit)
            NeoToolbarIcon(Icons.Rounded.MoreHoriz, "More", onMore)
        } else {
            NeoToolbarIcon(Icons.Rounded.ThumbUp, "Like", onLike)
            NeoToolbarIcon(Icons.Rounded.ThumbDown, "Dislike", onDislike)
            NeoToolbarIcon(Icons.Rounded.Refresh, "Regenerate", onRegenerate)
            NeoToolbarIcon(Icons.Rounded.Share, "Share", onShare)
            NeoToolbarIcon(Icons.Rounded.Edit, "Edit", onEdit)
            NeoToolbarIcon(Icons.Rounded.MoreHoriz, "More", onMore)
        }
    }
}

@Composable
private fun NeoToolbarIcon(icon: ImageVector, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .94f else 1f, spring(dampingRatio = .94f, stiffness = 520f), label = "message-toolbar")
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).scale(scale), interactionSource = interaction) {
        Icon(icon, description, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .90f))
    }
}

@Composable
fun StreamingCursor() {
    val transition = rememberInfiniteTransition(label = "streaming-cursor")
    val alpha by transition.animateFloat(.72f, .18f, infiniteRepeatable(tween(780, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "cursor-alpha")
    Box(Modifier.size(width = 2.dp, height = 17.dp).background(MaterialTheme.colorScheme.primary).alpha(alpha))
}
