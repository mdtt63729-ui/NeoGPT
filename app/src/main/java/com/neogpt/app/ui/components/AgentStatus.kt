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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.dp

/** Model-neutral status event used by the chat UI. */
enum class AgentStepState { IN_PROGRESS, COMPLETED, ERROR }

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
) {
    if (steps.isEmpty()) return
    var expanded by remember(steps.size) { mutableStateOf(false) }
    val active = steps.lastOrNull { it.state == AgentStepState.IN_PROGRESS } ?: steps.last()

    Column(
        modifier = modifier
            .animateContentSize(animationSpec = tween(260, easing = FastOutSlowInEasing)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StepStatusPill(
            step = active,
            expanded = expanded,
            canExpand = steps.size > 1 || active.state == AgentStepState.ERROR,
            onClick = {
                if (active.state == AgentStepState.ERROR) onErrorClick()
                else if (steps.size > 1) expanded = !expanded
            },
        )
        AnimatedVisibility(
            visible = expanded && steps.size > 1,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140)),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                steps.dropLast(1).asReversed().forEach { step ->
                    StepStatusPill(
                        step = step,
                        expanded = true,
                        canExpand = false,
                        onClick = {},
                        compact = true,
                    )
                }
            }
        }
    }
}

@Composable
fun StepStatusPill(
    step: AgentStep,
    expanded: Boolean = false,
    canExpand: Boolean = true,
    onClick: () -> Unit = {},
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(if (compact) 10.dp else 12.dp)
    Surface(
        modifier = Modifier
            .clickable(enabled = canExpand, onClick = onClick)
            .then(if (canExpand) Modifier else Modifier),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 5.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusGlyph(step.state, compact)
            Spacer(Modifier.width(8.dp))
            Text(
                text = step.message,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .liveRegion(LiveRegionMode.Polite),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            if (canExpand) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                    contentDescription = if (expanded) "Collapse status details" else "Expand status details",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusGlyph(state: AgentStepState, compact: Boolean) {
    when (state) {
        AgentStepState.IN_PROGRESS -> {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 11.dp else 12.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        AgentStepState.COMPLETED -> {
            Surface(
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        AgentStepState.ERROR -> {
            Surface(
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PulsingDotGrid(
    progress: Int,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "image-dot-grid")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "image-grid-phase",
    )
    val primary = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.Canvas(modifier) {
        val columns = 5
        val rows = 5
        val spacingX = size.width / (columns + 1)
        val spacingY = size.height / (rows + 1)
        val normalizedProgress = progress.coerceIn(0, 100) / 100f
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val index = row * columns + column
                val wave = ((phase * 2f + index * 0.09f) % 1f)
                val threshold = normalizedProgress * 25f
                val active = index < threshold
                val alpha = if (active) 0.28f + wave * 0.62f else 0.10f + wave * 0.12f
                val radius = 2.2.dp.toPx() * (if (active) 0.9f + wave * 0.55f else 0.82f)
                drawCircle(
                    color = primary.copy(alpha = alpha),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(spacingX * (column + 1), spacingY * (row + 1)),
                )
            }
        }
    }
}
