package com.neogpt.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

private const val DURATION = 360
private const val EXIT_DURATION = 300

fun AnimatedContentTransitionScope<*>.iosEnter() = slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(DURATION))
fun AnimatedContentTransitionScope<*>.iosExit() = slideOutHorizontally(tween(EXIT_DURATION, easing = FastOutSlowInEasing)) { -it / 3 } + fadeOut(tween(EXIT_DURATION))
fun AnimatedContentTransitionScope<*>.iosPopEnter() = slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -it / 3 } + fadeIn(tween(DURATION))
fun AnimatedContentTransitionScope<*>.iosPopExit() = slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(DURATION))

fun AnimatedContentTransitionScope<*>.slideUpEnter() = iosEnter()
fun AnimatedContentTransitionScope<*>.slideHorizontalEnter() = iosEnter()
fun AnimatedContentTransitionScope<*>.slideRightExit() = iosPopExit()
fun AnimatedContentTransitionScope<*>.fadeIn() = fadeIn(tween(DURATION))
fun AnimatedContentTransitionScope<*>.fadeOut() = fadeOut(tween(DURATION))
