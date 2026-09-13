package com.neogpt.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

// One restrained motion language across every screen. The curve is deliberately
// soft at the end so screens settle instead of snapping/flashing into place.
private val NeoEase = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
private const val ENTER_DURATION = 320
private const val EXIT_DURATION = 250

fun AnimatedContentTransitionScope<*>.iosEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(ENTER_DURATION, easing = NeoEase),
        initialOffsetX = { fullWidth -> fullWidth },
    ) + fadeIn(tween(ENTER_DURATION, easing = NeoEase), initialAlpha = 0.92f)

fun AnimatedContentTransitionScope<*>.iosExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(EXIT_DURATION, easing = NeoEase),
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
    ) + fadeOut(tween(EXIT_DURATION, easing = NeoEase), targetAlpha = 0.96f)

fun AnimatedContentTransitionScope<*>.iosPopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(ENTER_DURATION, easing = NeoEase),
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
    ) + fadeIn(tween(ENTER_DURATION, easing = NeoEase), initialAlpha = 0.96f)

fun AnimatedContentTransitionScope<*>.iosPopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(EXIT_DURATION, easing = NeoEase),
        targetOffsetX = { fullWidth -> fullWidth },
    ) + fadeOut(tween(EXIT_DURATION, easing = NeoEase), targetAlpha = 0.94f)

fun AnimatedContentTransitionScope<*>.slideUpEnter(): EnterTransition = iosEnter()
fun AnimatedContentTransitionScope<*>.slideHorizontalEnter(): EnterTransition = iosEnter()
fun AnimatedContentTransitionScope<*>.slideRightExit(): ExitTransition = iosPopExit()
fun AnimatedContentTransitionScope<*>.fadeIn(): EnterTransition = fadeIn(tween(ENTER_DURATION, easing = NeoEase))
fun AnimatedContentTransitionScope<*>.fadeOut(): ExitTransition = fadeOut(tween(EXIT_DURATION, easing = NeoEase))
