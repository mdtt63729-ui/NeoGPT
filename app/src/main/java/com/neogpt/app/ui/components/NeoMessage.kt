package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
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
) {
    var showToolbar by remember { mutableStateOf(false) }

    if (message.role == MessageRole.USER) {
        // User message — right aligned bubble
        Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.xs),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 320.dp),
                shape = NeoShapes.userBubble,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.md),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    } else {
        // AI message — left aligned, no bubble, with avatar
        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.sm),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                // AI Avatar
                Surface(
                    modifier = Modifier.size(NeoDimens.avatarSmall),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(NeoSpacing.md))
                // Content
                Column(modifier = Modifier.weight(1f)) {
                    if (message.agentSteps.isNotEmpty()) {
                        AgentStatusStack(
                            steps = message.agentSteps,
                            modifier = Modifier.padding(bottom = NeoSpacing.xs),
                            onErrorClick = onRegenerate,
                        )
                    }
                    if (message.content.isNotEmpty()) {
                        NeoMarkdown(
                            markdown = message.content,
                            isStreaming = message.isStreaming,
                        )
                    } else if (message.isStreaming && !message.isImageGenerating) {
                        NeoThinkingIndicator()
                    }
                    if (message.isImageGenerating || message.imageUrl != null) {
                        Spacer(modifier = Modifier.height(NeoSpacing.sm))
                        NeoImageGenerationCard(
                            imageUrl = message.imageUrl,
                            isGenerating = message.isImageGenerating,
                            imageCreated = message.imageCreated,
                            imageProgress = message.imageProgress,
                            imageStatusText = message.imageStatusText,
                            onDownload = onDownloadImage,
                        )
                    }
                    // Streaming cursor
                    if (message.isStreaming && message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(NeoSpacing.xs))
                        StreamingCursor()
                    }
                    // Toolbar
                    AnimatedVisibility(visible = showToolbar || !message.isStreaming) {
                        MessageToolbar(
                            onCopy = onCopy,
                            onRegenerate = onRegenerate,
                            onShare = onShare,
                            onEdit = onEdit,
                            onLike = onLike,
                            onDislike = onDislike,
                            onMore = onMore,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageToolbar(
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(top = NeoSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(NeoSpacing.xs),
    ) {
        NeoIconButton(icon = Icons.Rounded.ContentCopy, onClick = onCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.ThumbUp, onClick = onLike, contentDescription = "Like", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.ThumbDown, onClick = onDislike, contentDescription = "Dislike", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.Refresh, onClick = onRegenerate, contentDescription = "Regenerate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.Share, onClick = onShare, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.Edit, onClick = onEdit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        NeoIconButton(icon = Icons.Rounded.MoreHoriz, onClick = onMore, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StreamingCursor() {
    val cursorAlpha by animateFloatAsStateRepeated()
    Box(
        modifier = Modifier
            .size(width = 2.dp, height = 16.dp)
            .background(MaterialTheme.colorScheme.primary)
            .alpha(cursorAlpha)
    )
}

// Helper for cursor blink
@Composable
private fun animateFloatAsStateRepeated(): androidx.compose.runtime.State<Float> {
    val transition = rememberInfiniteTransition(label = "cursor")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
}
