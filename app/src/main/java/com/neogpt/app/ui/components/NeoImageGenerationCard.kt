package com.neogpt.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun NeoImageGenerationCard(
    imageUrl: String?,
    isGenerating: Boolean,
    imageCreated: Boolean,
    imageProgress: Int = 0,
    imageStatusText: String = "Sketching it out…",
    onDownload: () -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDownload by remember(imageUrl) { mutableStateOf(false) }
    val progress = imageProgress.coerceIn(0, 100)
    val imageAlpha by animateFloatAsState(
        targetValue = if (imageCreated) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "image-alpha",
    )
    val blurRadius by animateFloatAsState(
        targetValue = if (imageCreated) 0f else 12f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "image-blur",
    )

    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            if (isGenerating) {
                PulsingDotGrid(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth(0.64f)
                        .aspectRatio(1f),
                )
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(imageStatusText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.width(124.dp).height(2.dp))
                }
            }

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Generated image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = imageAlpha }
                        .blur(blurRadius.dp)
                        .pointerInput(imageUrl) {
                            detectTapGestures(onLongPress = { showDownload = true })
                        },
                    contentScale = ContentScale.Crop,
                )
            }

            AnimatedContent(
                targetState = showDownload && imageUrl != null,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "image-download-overlay",
            ) { visible ->
                if (visible) {
                    Surface(
                        onClick = { showDownload = false; onDownload() },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        tonalElevation = 2.dp,
                    ) {
                        Row(Modifier.padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Download, null, Modifier.size(19.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Download image", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = when {
                imageCreated -> 2
                isGenerating -> 1
                else -> 0
            },
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "image-caption",
        ) { state ->
            when (state) {
                2 -> Text("Image ready • press and hold to save", modifier = Modifier.padding(top = 9.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                1 -> Spacer(Modifier.height(9.dp))
                else -> if (imageUrl == null) {
                    TextButton(onClick = onRetry, modifier = Modifier.padding(top = 1.dp)) {
                        Icon(Icons.Rounded.Refresh, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Try again")
                    }
                }
            }
        }
    }
}
