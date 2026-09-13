package com.neogpt.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoSpacing

/**
 * Quiet, low-amplitude thinking treatment. It deliberately avoids bars, glows and
 * scale jumps so the response area never flashes while a stream is starting.
 */
@Composable
fun NeoThinkingIndicator(label: String = "Thinking", modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "neo-thinking")
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        repeat(3) { index ->
            val phase by transition.animateFloat(
                initialValue = 0.72f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(760, delayMillis = index * 120, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "thinking-dot-$index",
            )
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(6.dp)
                    .scale(0.86f + phase * 0.14f)
                    .alpha(0.42f + phase * 0.58f),
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.size(6.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {}
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
