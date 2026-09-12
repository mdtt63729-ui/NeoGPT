package com.neogpt.app.ui.animations

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

// ═══════════════════════════════════════════════════════════
// NEO GPT — SPRING SPECS
// ═══════════════════════════════════════════════════════════

object NeoSpring {
    val Default = spring<Float>(dampingRatio = 0.75f, stiffness = 400f)
    val Gentle = spring<Float>(dampingRatio = 0.9f, stiffness = 300f)
    val Snappy = spring<Float>(dampingRatio = 0.6f, stiffness = 500f)
    val Bouncy = spring<Float>(dampingRatio = 0.5f, stiffness = 350f)
}

object NeoMotion {
    const val SPLASH_DURATION = 900
    const val PAGE_TRANSITION = 350
    const val SHEET_DURATION = 300
    const val BUTTON_PRESS = 150
    const val THINKING_PULSE = 1200
    const val STREAMING_CHUNK = 50
}
