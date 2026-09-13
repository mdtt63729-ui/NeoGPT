package com.neogpt.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoThinkingIndicator(label: String = "Thinking", modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = NeoSpacing.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        repeat(5) { index ->
            val transition = rememberInfiniteTransition(label = "thinking-bar-$index")
            val height by transition.animateFloat(
                initialValue = 5f, targetValue = 17f,
                animationSpec = infiniteRepeatable(tween(560, delayMillis = index * 85, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "thinking-height-$index",
            )
            Surface(Modifier.width(3.dp).height(height.dp), color = MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp)) {}
        }
        androidx.compose.material3.Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
