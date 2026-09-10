package com.neogpt.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
// NEO GPT — SHAPE SYSTEM
// ═══════════════════════════════════════════════════════════

object NeoShapes {
    val none = RectangleShape
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val xlarge = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(28.dp)
    val bottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
    // Message bubble shapes
    val userBubble = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomEnd = 12.dp,
        bottomStart = 4.dp,
    )
    val aiBubble = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 12.dp,
        bottomEnd = 12.dp,
        bottomStart = 12.dp,
    )
}

val NeoMaterialShapes = Shapes(
    extraSmall = NeoShapes.small,
    small = NeoShapes.small,
    medium = NeoShapes.medium,
    large = NeoShapes.large,
    extraLarge = NeoShapes.xlarge,
)
