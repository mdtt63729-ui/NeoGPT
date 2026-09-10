package com.neogpt.app.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.neogpt.app.ui.components.NeoTopBar

@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        NeoTopBar(
            title = "Project Detail",
            onMenuClick = onBack,
            showBack = true,
            onBackClick = onBack,
        )
        Text(
            text = "Project: $projectId",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
