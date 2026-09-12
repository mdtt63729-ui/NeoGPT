package com.neogpt.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.neogpt.app.settings.rememberAppSettingsState
import com.neogpt.app.ui.theme.NeoGlassBorder
import com.neogpt.app.ui.theme.NeoOnSurfaceVariant

@Composable
fun NeoIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tint: Color = NeoOnSurfaceVariant,
) {
    val ui = rememberAppSettingsState(LocalContext.current)
    val haptic = LocalHapticFeedback.current
    val click = { if (ui.haptics) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey); onClick() }
    if (ui.liquidGlass) {
        val source = remember { MutableInteractionSource() }
        val pressed by source.collectIsPressedAsState()
        val scale by animateFloatAsState(if (ui.animations && pressed) .90f else 1f, spring(dampingRatio = .65f, stiffness = Spring.StiffnessMedium), label = "glassIconPress")
        Surface(
            modifier = modifier.size(48.dp).graphicsLayer { scaleX = scale; scaleY = scale },
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = .34f),
            border = BorderStroke(.7.dp, NeoGlassBorder.copy(alpha = .9f)),
            onClick = click,
            enabled = enabled,
            interactionSource = source,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription, Modifier.size(24.dp), tint = tint)
            }
        }
    } else {
        IconButton(onClick = click, modifier = modifier.size(48.dp), enabled = enabled, colors = IconButtonDefaults.iconButtonColors(contentColor = tint)) {
            Icon(icon, contentDescription, Modifier.size(24.dp), tint = tint)
        }
    }
}
