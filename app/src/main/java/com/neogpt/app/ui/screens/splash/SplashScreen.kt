package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fully in-app launch experience. No app icon, logo tile, or Android splash artwork is
 * rendered here; the OS starting window is intentionally kept visually neutral.
 */
@Composable
fun SplashScreen(
    onNavigate: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "neo-launch")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "orbit",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    var visible by remember { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(1450)
        onNavigate()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(300.dp).alpha(0.16f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.31f
            val a = Math.toRadians(orbit.toDouble())
            val glowCenter = Offset(
                center.x + cos(a).toFloat() * radius,
                center.y + sin(a).toFloat() * radius,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent),
                    center = glowCenter,
                    radius = size.minDimension * 0.34f,
                ),
                radius = size.minDimension * 0.34f,
                center = glowCenter,
            )
            drawArc(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                startAngle = orbit,
                sweepAngle = 105f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        AnimatedContent(
            targetState = visible,
            transitionSpec = { fadeIn(tween(650)) + scaleIn(tween(700), initialScale = 0.94f) togetherWith fadeOut(tween(200)) },
            label = "launch-content",
        ) { show ->
            if (show) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Neo GPT",
                        modifier = Modifier.scale(pulse),
                        style = MaterialTheme.typography.displayLarge,
                        fontFamily = NeoFontFamily,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(3) { index ->
                            val alpha = 0.35f + ((sin(Math.toRadians((orbit + index * 120f).toDouble())).toFloat() + 1f) / 2f) * 0.65f
                            Box(
                                Modifier
                                    .size(if (index == 1) 7.dp else 5.dp)
                                    .alpha(alpha)
                                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}
