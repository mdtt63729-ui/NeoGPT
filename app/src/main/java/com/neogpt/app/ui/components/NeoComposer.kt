package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Code
enum class ComposerMode {
    DEFAULT, THINKING, SEARCH, RESEARCH, CODE
}

data class AttachmentChip(
    val id: String,
    val name: String,
    val type: AttachmentType,
)

enum class AttachmentType { IMAGE, FILE, AUDIO, VIDEO }

@Composable
fun NeoComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onVoiceClick: () -> Unit = {},
    isGenerating: Boolean = false,
    onStop: () -> Unit = {},
    attachments: List<AttachmentChip> = emptyList(),
    activeMode: ComposerMode? = null,
    placeholder: String = "Ask anything…",
) {
    val sendScale by animateFloatAsState(
        targetValue = if (text.isNotBlank() || isGenerating) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "sendScale",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = NeoSpacing.lg)
            .padding(vertical = 2.dp),
        shape = NeoShapes.pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.xs),
        ) {
            // Attachment chips
            AnimatedVisibility(
                visible = attachments.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NeoSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(NeoSpacing.sm),
                ) {
                    attachments.forEach { chip ->
                        NeoFileChip(
                            name = chip.name,
                            type = chip.type,
                            onRemove = { /* handle remove */ },
                        )
                    }
                }
            }

            // Mode indicator
            AnimatedVisibility(
                visible = activeMode != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                if (activeMode != null) {
                    NeoModeChip(
                        mode = activeMode,
                        onClick = { /* toggle mode off */ },
                    )
                }
            }

            // Main input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = NeoDimens.composerMinHeight, max = NeoDimens.composerMaxHeight)
                    .wrapContentHeight(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Add button
                NeoIconButton(
                    icon = Icons.Rounded.Add,
                    onClick = onAddClick,
                    contentDescription = "Add attachment or tool",
                )

                // Text field
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    shape = NeoShapes.pill,
                )

                // Voice button
                NeoIconButton(
                    icon = Icons.Rounded.Mic,
                    onClick = onVoiceClick,
                    contentDescription = "Voice input",
                )

                Spacer(modifier = Modifier.width(NeoSpacing.xs))

                // Send / Stop button
                val sendIcon = if (isGenerating) Icons.Rounded.Stop else Icons.Rounded.ArrowUpward
                val sendColor = if (text.isNotBlank() || isGenerating)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            scaleX = sendScale
                            scaleY = sendScale
                        },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = sendColor,
                    onClick = if (isGenerating) onStop else if (text.isNotBlank()) onSend else ({}),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = sendIcon,
                            contentDescription = if (isGenerating) "Stop" else "Send",
                            tint = if (text.isNotBlank() || isGenerating)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeoModeChip(
    mode: ComposerMode,
    onClick: () -> Unit,
) {
    val label = when (mode) {
        ComposerMode.THINKING -> "Thinking"
        ComposerMode.SEARCH -> "Web Search"
        ComposerMode.RESEARCH -> "Research"
        ComposerMode.CODE -> "Code"
        ComposerMode.DEFAULT -> ""
    }
    val icon = when (mode) {
        ComposerMode.THINKING -> Icons.Rounded.AutoAwesome
        ComposerMode.SEARCH -> Icons.Rounded.Search
        ComposerMode.RESEARCH -> Icons.Rounded.TravelExplore
        ComposerMode.CODE -> Icons.Rounded.Code
        ComposerMode.DEFAULT -> null
    }
    Surface(
        shape = NeoShapes.pill,
        color = MaterialTheme.colorScheme.primaryContainer,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = NeoSpacing.md, vertical = NeoSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.width(NeoSpacing.xs))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
