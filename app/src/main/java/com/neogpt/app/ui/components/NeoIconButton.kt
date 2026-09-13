package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoOnSurfaceVariant

@Composable
fun NeoIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tint: Color = NeoOnSurfaceVariant,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
) {
    val haptic = LocalHapticFeedback.current
    IconButton(
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.VirtualKey); onClick() },
        modifier = modifier.size(42.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint)
    }
}
