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
import androidx.compose.ui.unit.dp

/** Quiet thinking treatment: text + three soft dots, never a spinning ring. */
@Composable
fun NeoThinkingIndicator(label: String = "Thinking", modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "neo-thinking")
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.22f,
                targetValue = 0.90f,
                animationSpec = infiniteRepeatable(
                    tween(900, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "thinking-dot-alpha-$index",
            )
            androidx.compose.foundation.layout.Box(Modifier.size(4.dp).alpha(alpha)) {
                androidx.compose.material3.Surface(Modifier.size(4.dp), androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.onSurfaceVariant) {}
            }
        }
    }
}
