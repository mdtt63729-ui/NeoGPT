package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing


enum class ComposerMode { DEFAULT, THINKING, SEARCH, RESEARCH, CODE }
data class AttachmentChip(val id: String, val name: String, val type: AttachmentType)
enum class AttachmentType { IMAGE, FILE, AUDIO, VIDEO }

@Composable
fun NeoComposer(
    text: String, onTextChange: (String) -> Unit, onSend: () -> Unit, onAddClick: () -> Unit, onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier, onVoiceClick: () -> Unit = {}, onVoiceStop: () -> Unit = {}, onVoiceCancel: () -> Unit = {},
    isListening: Boolean = false, voiceTranscript: String = "", voiceRmsLevel: Float = 0f, onLiveClick: () -> Unit = {},
    isGenerating: Boolean = false, onStop: () -> Unit = {}, attachments: List<AttachmentChip> = emptyList(),
    onRemoveAttachment: (String) -> Unit = {}, activeMode: ComposerMode? = null,
    placeholder: String = "Ask anything…", enterToSend: Boolean = true,
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val action = when { isGenerating -> "stop"; text.isNotBlank() -> "send"; else -> "live" }

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        tonalElevation = 2.dp,
        shadowElevation = 5.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            if (attachments.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    attachments.forEach { chip -> NeoFileChip(name = chip.name, type = chip.type, onRemove = { onRemoveAttachment(chip.id) }) }
                }
            }
            if (isListening) {
                VoiceRecordingBar(
                    transcript = voiceTranscript,
                    rmsLevel = voiceRmsLevel,
                    onCancel = onVoiceCancel,
                    onStop = onVoiceStop,
                    onSend = { onVoiceStop() },
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp, max = 156.dp).padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        minLines = 1,
                        maxLines = 7,
                        keyboardOptions = KeyboardOptions(imeAction = if (enterToSend) ImeAction.Send else ImeAction.Default),
                        keyboardActions = KeyboardActions(onSend = { if (enterToSend && text.isNotBlank()) onSend() }),
                        decorationBox = { inner ->
                            if (text.isBlank()) Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            inner()
                        },
                    )
                }
            }

            if (!isListening) Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ComposerIconButton(Icons.Rounded.Add, "Add", onClick = { showAddMenu = true })
                DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }, shape = NeoShapes.large) {
                    DropdownMenuItem(leadingIcon = { Icon(Icons.Rounded.AttachFile, null) }, text = { Text("Add file") }, onClick = { showAddMenu = false; onAddClick() })
                    DropdownMenuItem(leadingIcon = { Icon(Icons.Rounded.Image, null) }, text = { Text("Generate image") }, onClick = { showAddMenu = false; onImageClick() })
                }
                Spacer(Modifier.weight(1f))
                ComposerIconButton(
                    if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    if (isListening) "Stop voice input" else "Voice input",
                    onClick = if (isListening) onVoiceStop else onVoiceClick,
                )
                AnimatedContent(targetState = action, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "composer-action") { current ->
                    Surface(
                        modifier = Modifier.size(48.dp), shape = CircleShape,
                        color = if (current == "live") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        onClick = when (current) { "stop" -> onStop; "send" -> onSend; else -> onLiveClick },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                when (current) { "stop" -> Icons.Rounded.Stop; "send" -> Icons.Rounded.ArrowUpward; else -> Icons.Rounded.GraphicEq },
                                contentDescription = when (current) { "stop" -> "Stop generating"; "send" -> "Send"; else -> "Live conversation" },
                                modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceRecordingBar(
    transcript: String,
    rmsLevel: Float,
    onCancel: () -> Unit,
    onStop: () -> Unit,
    onSend: () -> Unit,
) {
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = rmsLevel.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(80),
        label = "voice-progress",
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = onCancel,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, "Cancel voice input", Modifier.size(22.dp))
            }
        }
        Box(Modifier.weight(1f).height(40.dp), contentAlignment = Alignment.Center) {
            NeoVoiceWaveform(
                rmsLevel = maxOf(animatedProgress, if (transcript.isNotBlank()) 0.12f else 0f),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = onStop,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Stop, "Stop voice input", Modifier.size(20.dp))
            }
        }
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            onClick = onSend,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowUpward, "Send voice message", Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ComposerIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium),
        label = "composer-button-scale",
    )
    Surface(
        modifier = Modifier.size(42.dp).scale(scale), shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (pressed) 0.42f else 0.24f)),
        onClick = onClick,
        interactionSource = interaction,
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, description, Modifier.size(21.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun NeoModeChip(mode: ComposerMode, onClick: () -> Unit) {
    val label = when (mode) { ComposerMode.THINKING -> "Thinking"; ComposerMode.SEARCH -> "Web Search"; ComposerMode.RESEARCH -> "Research"; ComposerMode.CODE -> "Code"; ComposerMode.DEFAULT -> "" }
    Surface(shape = NeoShapes.pill, color = MaterialTheme.colorScheme.primaryContainer, onClick = onClick) {
        Text(label, modifier = Modifier.padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs), style = MaterialTheme.typography.labelMedium)
    }
}
