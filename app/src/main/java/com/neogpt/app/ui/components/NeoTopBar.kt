package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onTitleClick: () -> Unit = {},
    showTitleSelector: Boolean = false,
    onActionClick: () -> Unit = {},
    actionIcon: ImageVector? = null,
    menuIcon: ImageVector = Icons.Rounded.Menu,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Surface(
                onClick = onTitleClick,
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (showTitleSelector) {
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Rounded.KeyboardArrowDown, null, Modifier.size(18.dp))
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = if (showBack) onBackClick else onMenuClick) {
                Icon(if (showBack) Icons.Rounded.ArrowBack else menuIcon, if (showBack) "Back" else "Menu")
            }
        },
        actions = {
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) { Icon(actionIcon, "Action") }
            } else {
                Spacer(Modifier.width(NeoSpacing.sm))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
