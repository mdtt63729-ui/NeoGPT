package com.neogpt.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoThinkingIndicator(label: String = "Thinking", modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "thinking")
    val pulse by transition.animateFloat(0.82f, 1.18f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pulse")
    val alpha by transition.animateFloat(0.45f, 1f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "alpha")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(2600), RepeatMode.Restart), label = "rotation")

    Row(modifier.padding(vertical = NeoSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(22.dp).graphicsLayer { rotationZ = rotation }.alpha(0.16f),
            ) {
                Box(Modifier.size(22.dp).scale(pulse), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Surface(Modifier.size(22.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {}
                }
            }
            Box(Modifier.size(7.dp).scale(pulse).alpha(alpha)) {
                androidx.compose.material3.Surface(Modifier.size(7.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {}
            }
        }
        Spacer(Modifier.width(NeoSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(NeoSpacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { index ->
                val phase = rememberInfiniteTransition(label = "thinking-dot-$index")
                val dotAlpha by phase.animateFloat(0.25f, 1f, infiniteRepeatable(tween(500, delayMillis = index * 140), RepeatMode.Reverse), label = "dot-alpha-$index")
                Box(Modifier.size(4.dp).alpha(dotAlpha)) {
                    androidx.compose.material3.Surface(Modifier.size(4.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {}
                }
            }
        }
    }
}
