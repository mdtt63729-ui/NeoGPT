package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class AgentStepState { PENDING, IN_PROGRESS, COMPLETED, ERROR }

data class AgentStep(
    val id: String,
    val message: String,
    val state: AgentStepState = AgentStepState.IN_PROGRESS,
)

@Composable
fun AgentStatusStack(
    steps: List<AgentStep>,
    modifier: Modifier = Modifier,
    onErrorClick: () -> Unit = {},
    toolLabels: List<String> = emptyList(),
) {
    if (steps.isEmpty()) return
    var expanded by remember(steps.size) { mutableStateOf(false) }
    val active = steps.lastOrNull { it.state == AgentStepState.IN_PROGRESS } ?: steps.last()

    Column(
        modifier = modifier.animateContentSize(tween(260, easing = FastOutSlowInEasing)),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = steps.size > 1 || active.state == AgentStepState.ERROR) {
                    if (active.state == AgentStepState.ERROR) onErrorClick() else expanded = !expanded
                },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.76f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)),
        ) {
            Column(Modifier.padding(horizontal = 13.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thoughts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    if (active.state == AgentStepState.IN_PROGRESS) NeoSoftThinkingDots()
                    Spacer(Modifier.weight(1f))
                    if (steps.size > 1) Icon(if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight, null, Modifier.size(18.dp))
                }
                Spacer(Modifier.height(5.dp))
                Text(active.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                if (toolLabels.isNotEmpty()) {
                    Spacer(Modifier.height(5.dp))
                    Text("Used ${toolLabels.size} tool${if (toolLabels.size == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        AnimatedVisibility(
            visible = expanded && steps.size > 1,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 8.dp)) {
                steps.forEach { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AgentStepGlyph(step.state)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            step.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
                toolLabels.forEach { label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(16.dp), CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {}
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun NeoSoftThinkingDots() {
    val transition = rememberInfiniteTransition(label = "agent-dots")
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val pulse by transition.animateFloat(
                0.70f, 1f,
                infiniteRepeatable(tween(820, delayMillis = index * 120, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "agent-dot-$index",
            )
            androidx.compose.foundation.layout.Box(Modifier.size(4.dp).scale(pulse).alpha(pulse)) {
                Surface(Modifier.size(4.dp), CircleShape, color = MaterialTheme.colorScheme.primary) {}
            }
        }
    }
}

@Composable
private fun AgentStepGlyph(state: AgentStepState) {
    when (state) {
        AgentStepState.PENDING -> Surface(Modifier.size(16.dp), CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {}
        AgentStepState.IN_PROGRESS -> NeoSoftThinkingDots()
        AgentStepState.COMPLETED -> Surface(Modifier.size(16.dp), CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Check, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.primary) }
        }
        AgentStepState.ERROR -> Surface(Modifier.size(16.dp), CircleShape, color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f)) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun PulsingDotGrid(progress: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "image-dot-grid")
    val phase by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "image-grid-phase",
    )
    val primary = MaterialTheme.colorScheme.primary
    val completed = (progress.coerceIn(0, 100) / 100f) * 25f
    androidx.compose.foundation.Canvas(modifier) {
        val columns = 5
        val rows = 5
        val spacingX = size.width / (columns + 1)
        val spacingY = size.height / (rows + 1)
        for (index in 0 until 25) {
            val row = index / columns
            val column = index % columns
            val distance = kotlin.math.abs(index - phase * 24f)
            val wave = (1f - (distance / 7f)).coerceIn(0f, 1f)
            val progressAlpha = if (index < completed) 0.72f else 0.18f
            val alpha = (progressAlpha + wave * 0.18f).coerceIn(0f, 1f)
            val radius = (1.9f + wave * 0.9f).dp.toPx()
            drawCircle(primary.copy(alpha = alpha), radius, androidx.compose.ui.geometry.Offset(spacingX * (column + 1), spacingY * (row + 1)))
        }
    }
}
