package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing

data class NeoModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val speedBadge: String,
    val contextWindow: String,
    val capabilities: List<String>,
)

@Composable
fun NeoModelPickerSheet(
    models: List<NeoModelInfo>,
    selectedModelId: String,
    onSelect: (NeoModelInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    NeoBottomSheet(
        onDismiss = onDismiss,
        title = "Choose model",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = NeoSpacing.lg),
        ) {
            models.forEach { model ->
                NeoModelItem(
                    model = model,
                    isSelected = model.id == selectedModelId,
                    onClick = { onSelect(model) },
                )
                Spacer(modifier = Modifier.height(NeoSpacing.sm))
            }
            Spacer(modifier = Modifier.height(NeoSpacing.md))
            Text(
                text = "Model IDs are configurable and may change based on API availability.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = NeoSpacing.md),
            )
            Spacer(modifier = Modifier.height(NeoSpacing.xxl))
        }
    }
}

@Composable
private fun NeoModelItem(
    model: NeoModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = NeoShapes.large,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(NeoSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(NeoSpacing.sm))
                    NeoChip(
                        text = model.speedBadge,
                        selected = false,
                    )
                }
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
                Text(
                    text = "Context: ${model.contextWindow}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}
