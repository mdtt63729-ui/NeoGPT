package com.neogpt.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

// ═══════════════════════════════════════════════════════════
// iOS-STYLE PAGE TRANSITIONS
// ═══════════════════════════════════════════════════════════

private const val DURATION = 350

// Home → Chat: Fade + slide up
fun AnimatedContentTransitionScope<*>.slideUpEnter() = slideInVertically(
    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetY = { it / 6 },
) + fadeIn(tween(DURATION))

// Chat → Project: Horizontal slide (iOS-style)
fun AnimatedContentTransitionScope<*>.slideHorizontalEnter() = slideInHorizontally(
    animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { it },
) + fadeIn(tween(DURATION))

// Back: slide right
fun AnimatedContentTransitionScope<*>.slideRightExit() = slideOutHorizontally(
    animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
    targetOffsetX = { it },
) + fadeOut(tween(DURATION))

// Fade
fun AnimatedContentTransitionScope<*>.fadeIn() = fadeIn(tween(DURATION))
fun AnimatedContentTransitionScope<*>.fadeOut() = fadeOut(tween(DURATION))
