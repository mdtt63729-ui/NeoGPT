package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoDimens
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.material.icons.rounded.Menu
@Composable
fun NeoTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onTitleClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    menuIcon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Rounded.Menu,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = NeoSpacing.sm, vertical = NeoSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeoIconButton(
            icon = if (showBack) Icons.Rounded.ArrowBack else menuIcon,
            onClick = if (showBack) onBackClick else onMenuClick,
            contentDescription = if (showBack) "Back" else "Menu",
        )

        Spacer(modifier = Modifier.width(NeoSpacing.sm))

        // Center: Model selector (tappable)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoIconButton(
                icon = Icons.Rounded.KeyboardArrowDown,
                onClick = onTitleClick,
                contentDescription = "Select model",
            )
            Spacer(modifier = Modifier.width(NeoSpacing.xs))
        }

        Spacer(modifier = Modifier.width(NeoSpacing.sm))

        if (actionIcon != null) {
            NeoIconButton(
                icon = actionIcon,
                onClick = onActionClick,
                contentDescription = "Action",
            )
        } else {
            Box(modifier = Modifier.size(48.dp))
        }
    }
}
