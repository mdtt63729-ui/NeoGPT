package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.neogpt.app.ui.theme.NeoSpacing

@OptIn(ExperimentalMaterial3Api::class)
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
    val ui = neoUiSettings()
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Surface(
                onClick = onTitleClick,
                shape = MaterialTheme.shapes.large,
                color = if (ui.liquidGlass) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                border = if (ui.liquidGlass) androidx.compose.foundation.BorderStroke(0.7.dp, Color.White.copy(alpha = 0.18f)) else null,
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
