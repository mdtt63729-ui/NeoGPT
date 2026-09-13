package com.neogpt.app.ui.screens.live

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.live.LiveConnectionState
import com.neogpt.app.live.LiveConversationViewModel
import com.neogpt.app.ui.components.NeoTopBar
import com.neogpt.app.ui.theme.NeoSpacing

/**
 * Native real-time Gemini voice conversation. The normal text-chat pipeline is
 * intentionally not reused here: Live uses a persistent bidirectional audio
 * WebSocket, AudioRecord input and AudioTrack output.
 */
@Composable
fun LiveConversationScreen(modelId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { LiveConversationViewModel(context) }
    val state by viewModel.state.collectAsState()
    val permissionGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
    var hasMicrophonePermission by remember { mutableStateOf(permissionGranted) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicrophonePermission = granted
        if (granted) viewModel.start()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stop() }
    }

    val running = state.connection == LiveConnectionState.CONNECTING || state.connection == LiveConnectionState.CONNECTED
    val canStart = state.connection == LiveConnectionState.IDLE || state.connection == LiveConnectionState.ERROR

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoTopBar(
                title = "Live Conversation",
                onMenuClick = {},
                showBack = true,
                onBackClick = { viewModel.stop(); onBack() },
                showTitleSelector = false,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = NeoSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            ) {
                Row(Modifier.padding(horizontal = 13.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(7.dp))
                    Text("Gemini Live • ${GeminiLiveModelLabel}", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.weight(0.32f))

            LiveOrb(
                active = running,
                speaking = state.status == "Speaking",
                userLevel = state.userLevel,
                assistantLevel = state.assistantLevel,
            )

            Spacer(Modifier.height(26.dp))

            AnimatedContent(
                targetState = state.status,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "live-status",
            ) { status ->
                Text(status, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                when {
                    state.error != null -> state.error.orEmpty()
                    state.connection == LiveConnectionState.CONNECTING -> "Opening a low-latency audio session…"
                    state.status == "Speaking" -> "You can interrupt Neo at any time."
                    running -> "Speak naturally. Gemini will listen, respond and keep the conversation going."
                    else -> "Start a real-time, voice-to-voice Gemini conversation."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)),
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Live transcript", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    if (state.userTranscript.isBlank() && state.assistantTranscript.isBlank()) {
                        Text("Your conversation will appear here while you speak.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (state.userTranscript.isNotBlank()) {
                                item { TranscriptBubble("You", state.userTranscript, true) }
                            }
                            if (state.assistantTranscript.isNotBlank()) {
                                item { TranscriptBubble("Neo", state.assistantTranscript, false) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            FilledTonalButton(
                onClick = {
                    if (running) {
                        viewModel.stop()
                    } else if (canStart) {
                        if (hasMicrophonePermission) viewModel.start()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(if (running) Icons.Rounded.StopCircle else Icons.Rounded.Mic, null)
                Spacer(Modifier.width(9.dp))
                Text(if (running) "End conversation" else "Start conversation")
            }
            Spacer(Modifier.height(NeoSpacing.md))
        }
    }
}

private const val GeminiLiveModelLabel = "3.1 Flash Live"

@Composable
private fun LiveOrb(
    active: Boolean,
    speaking: Boolean,
    userLevel: Float,
    assistantLevel: Float,
) {
    val transition = rememberInfiniteTransition(label = "live-orb")
    val rotation by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(5600, easing = FastOutSlowInEasing)),
        label = "live-orb-rotation",
    )
    val breathe by transition.animateFloat(
        0.97f,
        1.035f,
        infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "live-orb-breathe",
    )
    val energy = maxOf(userLevel, assistantLevel).coerceIn(0f, 1f)
    val targetScale = if (active) breathe + energy * 0.16f else 0.98f
    val scale by animateFloatAsState(targetScale, tween(180), label = "live-orb-scale")
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().scale(scale)) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.42f
            drawCircle(primary.copy(alpha = if (active) 0.12f else 0.07f), radius * 1.18f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        primary.copy(alpha = 0.82f),
                        secondary.copy(alpha = 0.52f),
                        tertiary.copy(alpha = 0.16f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 1.25f,
                ),
                radius = radius,
            )
            drawCircle(
                color = onPrimary.copy(alpha = 0.12f),
                radius = radius,
                style = Stroke(width = 1.5.dp.toPx()),
            )
            if (active) {
                rotate(rotation, center) {
                    drawArc(
                        color = onPrimary.copy(alpha = 0.82f),
                        startAngle = -28f,
                        sweepAngle = 76f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius * 1.16f, center.y - radius * 1.16f),
                        size = androidx.compose.ui.geometry.Size(radius * 2.32f, radius * 2.32f),
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }
            }
        }
        Surface(
            Modifier.size(132.dp).scale(scale),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f),
            tonalElevation = 7.dp,
            shadowElevation = 12.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (speaking) Icons.Rounded.GraphicEq else Icons.Rounded.Mic,
                    null,
                    Modifier.size(58.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun TranscriptBubble(label: String, text: String, user: Boolean) {
    Surface(
        color = if (user) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(11.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (user) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(3.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
