package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.animations.NeoMotion
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay

import androidx.compose.runtime.remember
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "splash")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    // Logo animation: 0% -> opacity 0, scale 0.85 -> 50% -> opacity 1, scale 1.05 -> 100% -> scale 1.0
    val animSpec = tween<Float>(NeoMotion.SPLASH_DURATION, easing = LinearEasing)
    val logoScale = remember { androidx.compose.animation.core.Animatable(0.85f) }
    val logoAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate opacity to 1
        logoAlpha.animateTo(1f, tween(NeoMotion.SPLASH_DURATION / 2))
        // Scale to 1.05 then settle to 1.0
        logoScale.animateTo(1.05f, tween(NeoMotion.SPLASH_DURATION / 2))
        logoScale.animateTo(1f, tween(200))
        delay(200)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        // Glow behind logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .alpha(glowAlpha)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape,
                )
                .scale(1.5f)
        )
        // Logo text
        Text(
            text = "Neo GPT",
            modifier = Modifier
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            style = MaterialTheme.typography.displayMedium,
            fontFamily = NeoFontFamily,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
