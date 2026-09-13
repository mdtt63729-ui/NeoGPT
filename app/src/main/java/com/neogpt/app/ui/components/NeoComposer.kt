package com.neogpt.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoShapes


enum class ComposerMode { DEFAULT, THINKING, SEARCH, RESEARCH, CODE }
data class AttachmentChip(val id: String, val name: String, val type: AttachmentType)
enum class AttachmentType { IMAGE, FILE, AUDIO, VIDEO }

@Composable
fun NeoComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAddClick: () -> Unit,
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onVoiceClick: () -> Unit = {},
    onVoiceStop: () -> Unit = {},
    onVoiceCancel: () -> Unit = {},
    isListening: Boolean = false,
    voiceTranscript: String = "",
    voiceRmsLevel: Float = 0f,
    onLiveClick: () -> Unit = {},
    isGenerating: Boolean = false,
    onStop: () -> Unit = {},
    attachments: List<AttachmentChip> = emptyList(),
    onRemoveAttachment: (String) -> Unit = {},
    activeMode: ComposerMode? = null,
    placeholder: String = "Ask anything…",
    enterToSend: Boolean = false,
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val action = when {
        isGenerating -> ComposerAction.STOP
        text.isNotBlank() || attachments.isNotEmpty() -> ComposerAction.SEND
        else -> ComposerAction.LIVE
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = 0.92f, stiffness = 420f)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)),
        tonalElevation = 1.dp,
        shadowElevation = 5.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (attachments.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    attachments.forEach { chip ->
                        NeoFileChip(name = chip.name, type = chip.type, onRemove = { onRemoveAttachment(chip.id) })
                    }
                }
            }

            if (isListening) {
                VoiceRecordingBar(
                    transcript = voiceTranscript,
                    rmsLevel = voiceRmsLevel,
                    onCancel = onVoiceCancel,
                    onStop = onVoiceStop,
                    onSend = onVoiceStop,
                )
            } else {
                // The field grows one measured line at a time. animateContentSize is
                // deliberately attached to the measured container rather than the
                // whole composer, preventing jumps while the IME is open.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp, max = 184.dp)
                        .animateContentSize(animationSpec = spring(dampingRatio = 0.94f, stiffness = 380f))
                        .padding(horizontal = 3.dp, vertical = 2.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        minLines = 1,
                        maxLines = 8,
                        keyboardOptions = KeyboardOptions(imeAction = if (enterToSend) ImeAction.Send else ImeAction.Default),
                        keyboardActions = KeyboardActions(onSend = { if (enterToSend && (text.isNotBlank() || attachments.isNotEmpty())) onSend() }),
                        decorationBox = { inner ->
                            if (text.isBlank()) {
                                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            inner()
                        },
                    )
                }
            }

            if (!isListening) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ComposerIconButton(Icons.Rounded.Add, "Add", onClick = { showAddMenu = true })
                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false },
                        shape = NeoShapes.large,
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.AttachFile, null) },
                            text = { Text("Add file") },
                            onClick = { showAddMenu = false; onAddClick() },
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Rounded.Image, null) },
                            text = { Text("Generate image") },
                            onClick = { showAddMenu = false; onImageClick() },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    ComposerIconButton(Icons.Rounded.Mic, "Voice input", onClick = onVoiceClick)
                    Spacer(Modifier.width(8.dp))
                    ComposerActionButton(action = action, onSend = onSend, onStop = onStop, onLive = onLiveClick)
                }
            }
        }
    }
}

private enum class ComposerAction { SEND, LIVE, STOP }

@Composable
private fun ComposerActionButton(action: ComposerAction, onSend: () -> Unit, onStop: () -> Unit, onLive: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 520f),
        label = "composer-action-scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (action == ComposerAction.LIVE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "composer-action-color",
    )
    Surface(
        onClick = when (action) {
            ComposerAction.SEND -> onSend
            ComposerAction.STOP -> onStop
            ComposerAction.LIVE -> onLive
        },
        modifier = Modifier.size(50.dp).scale(scale),
        shape = CircleShape,
        color = containerColor,
        interactionSource = interaction,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                when (action) {
                    ComposerAction.SEND -> Icons.Rounded.ArrowUpward
                    ComposerAction.STOP -> Icons.Rounded.Stop
                    ComposerAction.LIVE -> Icons.Rounded.GraphicEq
                },
                when (action) {
                    ComposerAction.SEND -> "Send"
                    ComposerAction.STOP -> "Stop generating"
                    ComposerAction.LIVE -> "Live conversation"
                },
                Modifier.size(23.dp),
                tint = if (action == ComposerAction.LIVE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun VoiceRecordingBar(transcript: String, rmsLevel: Float, onCancel: () -> Unit, onStop: () -> Unit, onSend: () -> Unit) {
    val animatedProgress by animateFloatAsState(
        targetValue = rmsLevel.coerceIn(0f, 1f),
        animationSpec = tween(110),
        label = "voice-progress",
    )
    Row(
        Modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ComposerIconButton(Icons.Rounded.Clear, "Cancel voice input", onCancel)
        Box(Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
            NeoVoiceWaveform(
                rmsLevel = maxOf(animatedProgress, if (transcript.isNotBlank()) 0.12f else 0f),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        VoiceStopButton(onClick = onStop)
        Surface(onClick = onSend, modifier = Modifier.size(46.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowUpward, "Send voice message", Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun VoiceStopButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "voice-stop-pulse")
    val pulse by transition.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "voice-stop-scale",
    )
    val halo by transition.animateFloat(
        .10f, .18f,
        infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "voice-stop-alpha",
    )
    Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(42.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .background(MaterialTheme.colorScheme.primary.copy(alpha = halo), CircleShape),
        )
        Surface(onClick = onClick, modifier = Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Stop, "Stop voice input", Modifier.size(19.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ComposerIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 560f),
        label = "composer-icon-scale",
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.size(46.dp).scale(scale),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        interactionSource = interaction,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, description, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}
