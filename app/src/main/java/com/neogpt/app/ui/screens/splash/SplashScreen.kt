package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neogpt.app.R
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay

/**
 * Neo GPT's own launch experience. Android's API 31+ system splash is intentionally
 * reduced to a transparent 1dp icon + matching background, so this is the only
 * perceptible splash surface and all brand motion happens here.
 */
@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "neo-splash-motion")

    val logoScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo-scale",
    )
    val logoRotation by transition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo-rotation",
    )
    val ringRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "ring-rotation",
    )
    val reverseRingRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(6200, easing = LinearEasing)),
        label = "reverse-ring-rotation",
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow-alpha",
    )
    val loadingSweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "loading-sweep",
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1650L)
        onNavigate()
    }

    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.10f),
                        background,
                        background,
                    ),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle animated ambient glow behind the brand mark.
        Box(
            Modifier
                .size(270.dp)
                .scale(logoScale)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.48f), Color.Transparent),
                    ),
                    CircleShape,
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(420)) + scaleIn(tween(700), initialScale = 0.78f),
            exit = fadeOut(tween(280)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp),
            ) {
                Box(Modifier.size(236.dp), contentAlignment = Alignment.Center) {
                    // Fine orbit lines keep the logo alive without making the launch screen busy.
                    Canvas(
                        Modifier
                            .size(218.dp)
                            .rotate(ringRotation)
                            .alpha(0.72f)
                    ) {
                        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = primary.copy(alpha = 0.18f),
                            radius = size.minDimension * 0.43f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f),
                        )
                        drawArc(
                            color = primary.copy(alpha = 0.78f),
                            startAngle = -32f,
                            sweepAngle = 74f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.07f, size.height * 0.07f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.86f, size.height * 0.86f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.4f),
                        )
                        drawCircle(
                            color = primary.copy(alpha = 0.72f),
                            center = androidx.compose.ui.geometry.Offset(center.x + size.width * 0.36f, center.y),
                            radius = 3.2f,
                        )
                    }
                    Canvas(
                        Modifier
                            .size(190.dp)
                            .rotate(reverseRingRotation)
                            .alpha(0.56f)
                    ) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.34f),
                            startAngle = 142f,
                            sweepAngle = 98f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.08f, size.height * 0.08f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.84f, size.height * 0.84f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.1f),
                        )
                    }

                    Box(
                        Modifier
                            .size(148.dp)
                            .scale(logoScale)
                            .rotate(logoRotation),
                        contentAlignment = Alignment.Center,
                    ) {
                        // The supplied high-resolution brand artwork is used directly.
                        Image(
                            painter = painterResource(R.drawable.neo_app_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.98f),
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    "Neo GPT",
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = NeoFontFamily,
                    color = onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Your intelligent voice-first assistant",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = NeoFontFamily,
                    color = onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))

                // Animated progress line: no static loading dots, just a clean premium motion cue.
                Box(
                    Modifier
                        .width(132.dp)
                        .height(3.dp)
                        .background(primary.copy(alpha = 0.13f), CircleShape)
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(loadingSweep)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(primary.copy(alpha = 0.18f), primary, primary.copy(alpha = 0.18f))
                                ),
                                CircleShape,
                            )
                    )
                }
            }
        }
    }
}
