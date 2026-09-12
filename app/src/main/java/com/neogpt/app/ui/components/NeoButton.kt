package com.neogpt.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing
import androidx.compose.ui.platform.LocalContext
import com.neogpt.app.settings.rememberAppSettingsState

import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
enum class NeoButtonStyle {
    Filled, Tonal, Outlined, Text, Glass
}

@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeoButtonStyle = NeoButtonStyle.Filled,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPill: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val appSettings = rememberAppSettingsState(LocalContext.current)
    val scale by animateFloatAsState(
        targetValue = if (appSettings.animations && isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "neoButtonScale",
    )
    val shape = if (isPill) NeoShapes.pill else NeoShapes.medium

    Row(modifier = modifier.graphicsLayerScale(scale)) {
        when (style) {
            NeoButtonStyle.Filled -> Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                contentPadding = PaddingValues(
                    horizontal = NeoSpacing.xxl,
                    vertical = NeoSpacing.md,
                ),
            ) {
                ButtonContent(text, icon)
            }
            NeoButtonStyle.Tonal -> FilledTonalButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
            ) {
                ButtonContent(text, icon)
            }
            NeoButtonStyle.Outlined -> OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
            ) {
                ButtonContent(text, icon)
            }
            NeoButtonStyle.Text -> TextButton(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
            ) {
                ButtonContent(text, icon)
            }
            NeoButtonStyle.Glass -> NeoGlassButton(
                text = text,
                onClick = onClick,
                modifier = Modifier,
                icon = icon,
                enabled = enabled,
                isPill = isPill,
            )
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(NeoSpacing.sm))
    }
    Text(text = text)
}

private fun Modifier.graphicsLayerScale(scale: Float): Modifier =
    this.scale(scale)
