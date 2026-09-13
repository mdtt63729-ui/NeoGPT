package com.neogpt.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** ECG-inspired speech trace whose amplitude follows SpeechRecognizer RMS input. */
@Composable
fun NeoVoiceWaveform(
    rmsLevel: Float,
    modifier: Modifier = Modifier,
) {
    val level by animateFloatAsState(
        targetValue = rmsLevel.coerceIn(0f, 1f),
        animationSpec = tween(90, easing = FastOutSlowInEasing),
        label = "voice-rms",
    )
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)

    Canvas(modifier.fillMaxWidth().height(38.dp)) {
        val centerY = size.height / 2f
        val width = size.width
        val amplitude = size.height * (0.08f + level * 0.36f)
        val cycleCount = 3.5f + level * 5.0f
        val cycleWidth = (width / cycleCount).coerceAtLeast(1f)
        val path = Path()

        fun ecgOffset(progress: Float): Float {
            return when {
                progress < 0.40f -> 0f
                progress < 0.445f -> -amplitude * 0.18f
                progress < 0.475f -> amplitude * 0.10f
                progress < 0.505f -> -amplitude * 1.00f
                progress < 0.535f -> amplitude * 0.78f
                progress < 0.57f -> -amplitude * 0.22f
                progress < 0.62f -> 0f
                progress < 0.69f -> -amplitude * 0.30f
                progress < 0.76f -> 0f
                else -> 0f
            }
        }

        val samples = 180
        for (i in 0..samples) {
            val x = width * i / samples
            val local = ((x % cycleWidth) / cycleWidth)
            val gentle = sin((x / width) * Math.PI * 2.0).toFloat() * amplitude * 0.025f
            val y = centerY + ecgOffset(local) + gentle
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawLine(
            color = track,
            start = androidx.compose.ui.geometry.Offset(0f, centerY),
            end = androidx.compose.ui.geometry.Offset(width, centerY),
            strokeWidth = 1f,
        )
        drawPath(
            path = path,
            color = primary,
            style = Stroke(width = 2.25f, cap = StrokeCap.Round),
        )
    }
}
