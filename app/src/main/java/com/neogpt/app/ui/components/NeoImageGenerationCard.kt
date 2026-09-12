package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neogpt.app.ui.theme.NeoSpacing
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun NeoImageGenerationCard(
    imageUrl: String?,
    isGenerating: Boolean,
    imageCreated: Boolean,
    onDownload: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDownload by remember(imageUrl) { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "image-generation")
    val pulse by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "image-pulse",
    )
    val shimmer by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "image-shimmer",
    )
    val blurRadius by animateFloatAsStateCompat(if (imageCreated) 0f else 16f)
    val imageAlpha by animateFloatAsStateCompat(if (imageCreated) 1f else 0f)
    val imageScale by animateFloatAsStateCompat(if (imageCreated) 1f else 0.96f)

    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (isGenerating || imageUrl == null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                ),
                                startX = shimmer * 700f,
                                endX = shimmer * 700f + 900f,
                            )
                        )
                )
                Surface(
                    modifier = Modifier.size(92.dp).graphicsLayer { scaleX = pulse; scaleY = pulse },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                ) {}
                Surface(
                    modifier = Modifier.size(62.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Text(
                    "Creating image…",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Generated image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = imageAlpha; scaleX = imageScale; scaleY = imageScale }
                        .blur(blurRadius.dp)
                        .pointerInput(imageUrl) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val heldForTwoSeconds = withTimeoutOrNull(2000L) {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: return@withTimeoutOrNull false
                                        if (change.changedToUp()) return@withTimeoutOrNull false
                                    }
                                } == null
                                if (heldForTwoSeconds) showDownload = true
                            }
                        },
                    contentScale = ContentScale.Crop,
                )
            }

            AnimatedContent(
                targetState = showDownload && imageUrl != null,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "download-overlay",
            ) { visible ->
                if (visible) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
                        shape = NeoSpacingShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        tonalElevation = 4.dp,
                        onClick = { showDownload = false; onDownload() },
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Download, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download image", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
        AnimatedContent(targetState = imageCreated, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "image-status") { created ->
            if (created) {
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Image, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(7.dp))
                    Text("Image created 🖼️", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            } else if (isGenerating) {
                Text("Generating your image…", Modifier.padding(top = 10.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (imageCreated) {
            Text("Press and hold the image for 2 seconds to download", Modifier.padding(top = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!isGenerating && imageUrl == null) {
            TextButton(onClick = onRetry, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text("Try again")
            }
        }
    }
}

private val NeoSpacingShape = RoundedCornerShape(16.dp)

@Composable
private fun animateFloatAsStateCompat(target: Float): Float {
    val value by androidx.compose.animation.core.animateFloatAsState(target, animationSpec = tween(650, easing = FastOutSlowInEasing), label = "image-state")
    return value
}
