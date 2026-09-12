package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoError
import com.neogpt.app.ui.theme.NeoSpacing

enum class NeoErrorType {
    GENERIC, NETWORK, RATE_LIMIT
}

@Composable
fun NeoErrorView(
    errorType: NeoErrorType = NeoErrorType.GENERIC,
    onRetry: () -> Unit = {},
    onChangeModel: () -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val message = when (errorType) {
        NeoErrorType.GENERIC -> "Something went wrong."
        NeoErrorType.NETWORK -> "Check your internet connection and try again."
        NeoErrorType.RATE_LIMIT -> "This model is temporarily unavailable."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NeoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = NeoError,
        )
        Spacer(modifier = Modifier.height(NeoSpacing.lg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(NeoSpacing.lg))
        Row(
            horizontalArrangement = Arrangement.spacedBy(NeoSpacing.md),
        ) {
            NeoButton(text = "Retry", onClick = onRetry, style = NeoButtonStyle.Tonal)
            if (errorType == NeoErrorType.RATE_LIMIT) {
                NeoButton(text = "Change model", onClick = onChangeModel, style = NeoButtonStyle.Text)
            }
            NeoButton(text = "Cancel", onClick = onCancel, style = NeoButtonStyle.Text)
        }
    }
}
