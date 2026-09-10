package com.neogpt.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoSkeleton(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 8,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .alpha(alpha),
    )
}

@Composable
fun NeoSkeletonList(
    itemCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier.padding(NeoSpacing.lg)) {
        repeat(itemCount) {
            NeoSkeleton(
                modifier = Modifier.fillMaxWidth().height(20.dp),
            )
            Spacer(modifier = Modifier.height(NeoSpacing.sm))
            NeoSkeleton(
                modifier = Modifier.fillMaxWidth(0.7f).height(16.dp),
            )
            Spacer(modifier = Modifier.height(NeoSpacing.lg))
        }
    }
}
