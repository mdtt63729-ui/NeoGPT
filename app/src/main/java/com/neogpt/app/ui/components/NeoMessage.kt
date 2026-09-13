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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.domain.model.Attachment
import com.neogpt.app.ui.theme.NeoSpacing

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
        Column(modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 5.dp), horizontalAlignment = Alignment.End) {
            Surface(
                modifier = Modifier.widthIn(max = 340.dp).combinedClickable(onClick = { userActions = !userActions }, onLongClick = { userActions = true }).animateContentSize(spring(dampingRatio = .9f, stiffness = 420f)),
                shape = RoundedCornerShape(21.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (message.content.isNotBlank()) Text(message.content, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    message.attachments.forEach { attachment ->
                        if (message.content.isNotBlank()) Spacer(Modifier.height(8.dp))
                        NeoAttachmentCard(attachment.name, attachment.mimeType, attachment.sizeBytes, { onOpenAttachment(attachment) })
                    }
                }
            }
            AnimatedVisibility(userActions, enter = fadeIn(tween(150)), exit = fadeOut(tween(100))) {
                MessageToolbar(user = true, onCopy, onRegenerate, onShare, onEdit, onLike, onDislike, onMore)
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp).animateContentSize(spring(dampingRatio = .92f, stiffness = 380f)),
        ) {
            if (message.content.isNotEmpty()) NeoMarkdown(message.content, isStreaming = message.isStreaming, textScale = responseTextScale)
            else if (message.isStreaming && !message.isImageGenerating) NeoThinkingIndicator()
            if (message.isImageGenerating || message.imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                NeoImageGenerationCard(message.imageUrl, message.isImageGenerating, message.imageCreated, message.imageProgress, message.imageStatusText, onDownloadImage)
            }
            if (message.isStreaming && message.content.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                StreamingCursor()
            }
            AnimatedVisibility(message.content.isNotBlank() && !message.isStreaming, enter = fadeIn(tween(160)), exit = fadeOut(tween(100))) {
                MessageToolbar(user = false, onCopy, onRegenerate, onShare, onEdit, onLike, onDislike, onMore)
            }
        }
    }
}

@Composable
private fun MessageToolbar(
    user: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onMore: () -> Unit,
) {
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
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .92f else 1f, spring(dampingRatio = .88f, stiffness = 520f), label = "message-toolbar")
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).scale(scale), interactionSource = interaction) { Icon(icon, description, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .92f)) }
}

@Composable
fun StreamingCursor() {
    val transition = rememberInfiniteTransition(label = "streaming-cursor")
    val alpha by transition.animateFloat(.85f, .22f, infiniteRepeatable(tween(720, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "cursor-alpha")
    Box(Modifier.size(width = 2.dp, height = 17.dp).background(MaterialTheme.colorScheme.primary).alpha(alpha))
}
