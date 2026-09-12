package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    modifier: Modifier = Modifier, onVoiceClick: () -> Unit = {}, onVoiceStop: () -> Unit = {},
    isListening: Boolean = false, voiceTranscript: String = "", onLiveClick: () -> Unit = {},
    isGenerating: Boolean = false, onStop: () -> Unit = {}, attachments: List<AttachmentChip> = emptyList(),
    onRemoveAttachment: (String) -> Unit = {}, activeMode: ComposerMode? = null,
    placeholder: String = "Ask anything…",
    enterToSend: Boolean = true,
) {
    val ui = neoUiSettings()
    if (ui.liquidGlass) {
        NeoLiquidGlass(modifier = modifier.fillMaxWidth().padding(horizontal = NeoSpacing.sm), shape = NeoShapes.pill) {
            ComposerContent(text, onTextChange, onSend, onAddClick, onImageClick, modifier, onVoiceClick, onVoiceStop, isListening, voiceTranscript, onLiveClick, isGenerating, onStop, attachments, onRemoveAttachment, activeMode, placeholder, enterToSend)
        }
    } else Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = NeoSpacing.sm), shape = NeoShapes.pill,
        color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 1.dp, shadowElevation = 2.dp,
    ) {
        ComposerContent(text, onTextChange, onSend, onAddClick, onImageClick, modifier, onVoiceClick, onVoiceStop, isListening, voiceTranscript, onLiveClick, isGenerating, onStop, attachments, onRemoveAttachment, activeMode, placeholder, enterToSend)
    }
}

@Composable
private fun ComposerContent(
    text: String, onTextChange: (String) -> Unit, onSend: () -> Unit, onAddClick: () -> Unit, onImageClick: () -> Unit,
    modifier: Modifier, onVoiceClick: () -> Unit, onVoiceStop: () -> Unit, isListening: Boolean, voiceTranscript: String,
    onLiveClick: () -> Unit, isGenerating: Boolean, onStop: () -> Unit, attachments: List<AttachmentChip>,
    onRemoveAttachment: (String) -> Unit, activeMode: ComposerMode?, placeholder: String, enterToSend: Boolean,
) {
        Column(Modifier.fillMaxWidth().padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.xs)) {
            AnimatedVisibility(attachments.isNotEmpty(), enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = NeoSpacing.xs, vertical = NeoSpacing.xs), horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm)) {
                    attachments.forEach { chip -> NeoFileChip(chip.name, chip.type, { onRemoveAttachment(chip.id) }) }
                }
            }
            AnimatedVisibility(activeMode != null, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                activeMode?.let { NeoModeChip(it, {}) }
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = NeoDimens.composerMinHeight, max = NeoDimens.composerMaxHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    NeoIconButton(icon = Icons.Rounded.Add, onClick = { showAddMenu = true }, contentDescription = "Add")
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
                }
                if (isListening) {
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp), verticalArrangement = Arrangement.Center) {
                        Text(if (voiceTranscript.isBlank()) "Listening…" else voiceTranscript, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 2)
                        Spacer(Modifier.height(2.dp))
                        Text("Speak naturally · stops after 2 seconds of silence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    OutlinedTextField(
                        value = text, onValueChange = onTextChange, modifier = Modifier.weight(1f),
                        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }, maxLines = 5,
                        keyboardOptions = KeyboardOptions(imeAction = if (enterToSend) ImeAction.Send else ImeAction.Default),
                        keyboardActions = KeyboardActions(onSend = { if (enterToSend && text.isNotBlank()) onSend() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ), shape = NeoShapes.pill,
                    )
                }
                if (isListening) NeoIconButton(icon = Icons.Rounded.Stop, onClick = onVoiceStop, contentDescription = "Stop voice input")
                else NeoIconButton(icon = Icons.Rounded.Mic, onClick = onVoiceClick, contentDescription = "Voice input")
                Spacer(Modifier.width(NeoSpacing.xs))
                val action = when { isGenerating -> "stop"; text.isNotBlank() -> "send"; else -> "live" }
                AnimatedContent(targetState = action, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "composer-action") { current ->
                    Surface(
                        modifier = Modifier.size(42.dp).graphicsLayer { scaleX = actionScale; scaleY = actionScale },
                        shape = CircleShape,
                        color = if (current == "live") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                        onClick = when (current) { "stop" -> onStop; "send" -> onSend; else -> onLiveClick },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                when (current) { "stop" -> Icons.Rounded.Stop; "send" -> Icons.Rounded.ArrowUpward; else -> Icons.Rounded.GraphicEq },
                                contentDescription = when (current) { "stop" -> "Stop generating"; "send" -> "Send"; else -> "Live conversation" },
                                tint = if (current == "live") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                    }
                }
            }
    }
}

@Composable
fun NeoModeChip(mode: ComposerMode, onClick: () -> Unit) {
    val label = when (mode) { ComposerMode.THINKING -> "Thinking"; ComposerMode.SEARCH -> "Web Search"; ComposerMode.RESEARCH -> "Research"; ComposerMode.CODE -> "Code"; ComposerMode.DEFAULT -> "" }
    val icon = when (mode) { ComposerMode.THINKING -> Icons.Rounded.AutoAwesome; ComposerMode.SEARCH -> Icons.Rounded.Search; ComposerMode.RESEARCH -> Icons.Rounded.TravelExplore; ComposerMode.CODE -> Icons.Rounded.Code; ComposerMode.DEFAULT -> null }
    Surface(shape = NeoShapes.pill, color = MaterialTheme.colorScheme.primaryContainer, onClick = onClick) {
        Row(Modifier.padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
            icon?.let { Icon(it, null, Modifier.size(16.dp)) }
            if (icon != null) Spacer(Modifier.width(NeoSpacing.xs))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
