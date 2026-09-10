package com.neogpt.app.ui.components

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoGlass
import com.neogpt.app.ui.theme.NeoGlassBorder
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
@Composable
fun NeoGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPill: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "glassScale",
    )
    val shape = if (isPill) NeoShapes.pill else NeoShapes.large

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = shape,
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) NeoGlass else Color(0xFF222226),
        border = BorderStroke(0.5.dp, NeoGlassBorder),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = NeoSpacing.xxl,
                    vertical = NeoSpacing.md,
                )
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(NeoSpacing.sm))
            }
            Text(text = text, color = Color.White)
        }
    }
}

@Composable
fun NeoGlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Int = 48,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "glassIconScale",
    )

    Surface(
        modifier = modifier
            .size(size.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = androidx.compose.foundation.shape.CircleShape,
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) NeoGlass else Color(0xFF1C1C1F),
        border = BorderStroke(0.5.dp, NeoGlassBorder),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(12.dp),
            tint = Color.White,
        )
    }
}
