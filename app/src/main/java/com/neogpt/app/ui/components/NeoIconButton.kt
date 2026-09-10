package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = tint,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
    }
}
