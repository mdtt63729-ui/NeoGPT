package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neogpt.app.R
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay
import kotlin.math.sin

/** Premium in-app launch animation. The Android starting window remains visually neutral. */
@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "neo-launch")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo-pulse",
    )
    val rotation by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo-rotation",
    )
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dots",
    )

    LaunchedEffect(Unit) {
        delay(1450L)
        onNavigate()
    }

    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    val iconPainter = painterResource(R.drawable.neo_app_icon)

    Box(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .scale(pulse)
                    .graphicsLayer { rotationZ = rotation },
            )
            Spacer(Modifier.height(26.dp))
            Text(
                text = "Neo GPT",
                color = onBackground,
                style = MaterialTheme.typography.displaySmall,
                fontFamily = NeoFontFamily,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { index ->
                    val phase = (shimmer + index * 0.28f) % 1f
                    val alpha = 0.28f + sin(phase * Math.PI).toFloat() * 0.72f
                    Box(
                        Modifier
                            .size(if (index == 1) 7.dp else 5.dp)
                            .alpha(alpha)
                            .background(primary, CircleShape),
                    )
                }
            }
        }
    }
}
