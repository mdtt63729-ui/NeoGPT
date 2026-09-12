package com.neogpt.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neogpt.app.R
import com.neogpt.app.ui.theme.NeoFontFamily
import kotlinx.coroutines.delay

/** Material 3 launch surface: tonal container, elevated brand mark and restrained motion. */
@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "splash-motion")
    val pulse by transition.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val rotate by transition.animateFloat(-2f, 2f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "rotate")
    val ringAlpha by transition.animateFloat(0.25f, 0.55f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "ring-alpha")
    LaunchedEffect(Unit) {
        visible = true
        delay(1350L)
        onNavigate()
    }
    val primary = MaterialTheme.colorScheme.primary
    val container = MaterialTheme.colorScheme.primaryContainer
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible, enter = fadeIn(tween(420)) + scaleIn(tween(520), initialScale = 0.88f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(178.dp), contentAlignment = Alignment.Center) {
                    Surface(
                        Modifier.size(166.dp).alpha(ringAlpha).rotate(rotate),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, primary.copy(alpha = 0.45f)),
                    ) {}
                    Surface(
                        Modifier.size(140.dp).scale(pulse),
                        shape = CircleShape,
                        color = container,
                        tonalElevation = 5.dp,
                        shadowElevation = 12.dp,
                    ) {
                        Box(Alignment.Center) { Image(painterResource(R.drawable.neo_app_icon), null, Modifier.size(116.dp)) }
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text("Neo GPT", style = MaterialTheme.typography.headlineLarge, fontFamily = NeoFontFamily)
                Spacer(Modifier.height(8.dp))
                Text("Your intelligent voice-first assistant", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        val alpha by transition.animateFloat(0.28f, 1f, infiniteRepeatable(tween(900, delayMillis = index * 180), RepeatMode.Reverse), label = "dot-$index")
                        Surface(Modifier.size(if (index == 1) 7.dp else 5.dp).alpha(alpha), shape = CircleShape, color = primary) {}
                    }
                }
            }
        }
    }
}
