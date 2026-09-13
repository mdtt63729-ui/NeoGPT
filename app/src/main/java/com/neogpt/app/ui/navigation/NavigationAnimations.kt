package com.neogpt.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope

// One calm motion language. No overshoot, no snap, and no stacked long animations.
private val NeoEase = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
private const val ENTER_DURATION = 280
private const val EXIT_DURATION = 220

fun AnimatedContentTransitionScope<*>.iosEnter(): EnterTransition =
    slideInHorizontally(tween(ENTER_DURATION, easing = NeoEase)) { it / 5 } +
        fadeIn(tween(ENTER_DURATION, easing = NeoEase), initialAlpha = 0.98f)

fun AnimatedContentTransitionScope<*>.iosExit(): ExitTransition =
    slideOutHorizontally(tween(EXIT_DURATION, easing = NeoEase)) { -it / 6 } +
        fadeOut(tween(EXIT_DURATION, easing = NeoEase), targetAlpha = 0.98f)

fun AnimatedContentTransitionScope<*>.iosPopEnter(): EnterTransition =
    slideInHorizontally(tween(ENTER_DURATION, easing = NeoEase)) { -it / 6 } +
        fadeIn(tween(ENTER_DURATION, easing = NeoEase), initialAlpha = 0.98f)

fun AnimatedContentTransitionScope<*>.iosPopExit(): ExitTransition =
    slideOutHorizontally(tween(EXIT_DURATION, easing = NeoEase)) { it / 5 } +
        fadeOut(tween(EXIT_DURATION, easing = NeoEase), targetAlpha = 0.98f)

fun AnimatedContentTransitionScope<*>.slideUpEnter(): EnterTransition = iosEnter()
fun AnimatedContentTransitionScope<*>.slideHorizontalEnter(): EnterTransition = iosEnter()
fun AnimatedContentTransitionScope<*>.slideRightExit(): ExitTransition = iosPopExit()
fun AnimatedContentTransitionScope<*>.fadeIn(): EnterTransition = fadeIn(tween(ENTER_DURATION, easing = NeoEase))
fun AnimatedContentTransitionScope<*>.fadeOut(): ExitTransition = fadeOut(tween(EXIT_DURATION, easing = NeoEase))
