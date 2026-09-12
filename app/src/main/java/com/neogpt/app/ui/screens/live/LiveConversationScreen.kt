package com.neogpt.app.ui.screens.live

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.components.NeoTopBar
import com.neogpt.app.ui.theme.NeoSpacing

/** Premium Live Conversation UI. Gemini Live transport/audio will be connected in the next implementation step. */
@Composable
fun LiveConversationScreen(modelId: String, onBack: () -> Unit) {
    var active by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "live-orb")
    val pulse by transition.animateFloat(0.94f, 1.08f, infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "orb-pulse")
    val ring by transition.animateFloat(0.85f, 1.18f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "orb-ring")
    val primary = MaterialTheme.colorScheme.primary
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { NeoTopBar("Live · ${modelId.removePrefix("gemini-").replace('-', ' ').replaceFirstChar { it.uppercase() }}", onMenuClick = {}, showBack = true, onBackClick = onBack, actionIcon = Icons.Rounded.MoreVert) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = NeoSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.7f))
            Box(Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().scale(if (active) ring else 1f)) {
                    drawCircle(primary.copy(alpha = 0.08f), radius = size.minDimension * 0.46f, style = Stroke(width = 2.dp.toPx()))
                }
                Surface(Modifier.size(184.dp).scale(if (active) pulse else 1f), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 6.dp, shadowElevation = 10.dp) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.GraphicEq, null, Modifier.size(76.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            }
            Spacer(Modifier.height(30.dp))
            AnimatedContent(targetState = active, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "live-state") { running ->
                Text(if (running) "Listening" else "Ready to talk", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (active) "Your live conversation will appear here as AI speaks." else "Start a live Gemini conversation with a fluid voice-first experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 1.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI transcript", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(if (active) "I'm listening. Speak naturally…" else "Your AI responses will appear here.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            Spacer(Modifier.weight(1f))
            FilledTonalButton(onClick = { active = !active }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(if (active) Icons.Rounded.StopCircle else Icons.Rounded.Mic, null)
                Spacer(Modifier.width(8.dp))
                Text(if (active) "End conversation" else "Start conversation")
            }
            Spacer(Modifier.height(NeoSpacing.md))
        }
    }
}
