package com.neogpt.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * Low-overhead voice waveform inspired by modern voice-recording composers.
 * RMS input controls the live amplitude while a subtle phase animation keeps
 * the trace alive between SpeechRecognizer callbacks.
 */
@Composable
fun NeoVoiceWaveform(
    rmsLevel: Float,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "voice-wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "voice-phase",
    )
    val level = rmsLevel.coerceIn(0f, 1f)
    val active = MaterialTheme.colorScheme.onSurface
    val inactive = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)

    Canvas(modifier.fillMaxWidth().height(34.dp)) {
        drawVoiceBars(level, phase, active, inactive)
    }
}

private fun DrawScope.drawVoiceBars(
    level: Float,
    phase: Float,
    active: Color,
    inactive: Color,
) {
    val count = 27
    val gap = 4.dp.toPx()
    val barWidth = ((size.width - gap * (count - 1)) / count).coerceAtLeast(2.dp.toPx())
    val centerY = size.height / 2f
    val maxHeight = size.height * 0.88f

    repeat(count) { index ->
        val x = index * (barWidth + gap)
        val normalized = index / (count - 1f)
        val centerWeight = 1f - abs(normalized - 0.5f) * 1.7f
        val wave = (0.5f + 0.5f * sin(phase + index * 0.72f)).coerceIn(0f, 1f)
        val base = 0.10f + level * (0.25f + centerWeight.coerceAtLeast(0f) * 0.62f)
        val height = (maxHeight * (base + wave * level * 0.18f)).coerceIn(4.dp.toPx(), maxHeight)
        val color = if (level > 0.025f && height > 8.dp.toPx()) active else inactive
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(x, centerY - height / 2f),
            size = androidx.compose.ui.geometry.Size(barWidth, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f),
        )
    }
}
