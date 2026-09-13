package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neogpt.app.R
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay

/**
 * Neo GPT's in-app launch experience. The navigation graph opens Home immediately;
 * MainActivity places this Material 3 surface over the app for a short, fully animated
 * handoff. The framework startup icon is blank so there is no second visible logo.
 */
@Composable
fun SplashScreen(onNavigate: () -> Unit, embedded: Boolean = false) {
    var visible by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "neo-splash-motion")

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
        delay(if (embedded) 720L else 1200L)
        onNavigate()
    }

    val background = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle animated ambient glow behind the brand mark.
        Box(
            Modifier
                .size(260.dp)
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
            enter = fadeIn(tween(360)) + slideInVertically(tween(520)) { it / 14 },
            exit = fadeOut(tween(280)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 28.dp),
            ) {
                Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.neo_app_icon),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
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
