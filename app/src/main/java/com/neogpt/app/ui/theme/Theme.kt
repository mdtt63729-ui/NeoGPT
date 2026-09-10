package com.neogpt.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════
// NEO GPT — COLOR SCHEMES
// ═══════════════════════════════════════════════════════════

private val NeoDarkColorScheme = darkColorScheme(
    primary = NeoPrimary,
    onPrimary = NeoOnPrimary,
    primaryContainer = NeoPrimaryContainer,
    onPrimaryContainer = NeoOnPrimaryContainer,
    background = NeoBackground,
    onBackground = NeoOnSurface,
    surface = NeoSurface,
    onSurface = NeoOnSurface,
    surfaceVariant = NeoSurfaceVariant,
    onSurfaceVariant = NeoOnSurfaceVariant,
    surfaceTint = NeoPrimary,
    outline = NeoOutline,
    outlineVariant = NeoOutlineVariant,
    error = NeoError,
    onError = NeoOnError,
    errorContainer = NeoErrorContainer,
)

private val NeoAmoledColorScheme = NeoDarkColorScheme.copy(
    background = NeoBackgroundAmoled,
    surface = NeoSurfaceAmoled,
)

private val NeoLightColorScheme = lightColorScheme(
    primary = NeoPrimaryLight,
    onPrimary = NeoOnPrimary,
    primaryContainer = NeoPrimaryContainer,
    onPrimaryContainer = NeoOnPrimaryContainer,
    background = NeoBackgroundLight,
    onBackground = NeoOnSurfaceLight,
    surface = NeoSurfaceLight,
    onSurface = NeoOnSurfaceLight,
    surfaceVariant = NeoSurfaceVariantLight,
    onSurfaceVariant = NeoOnSurfaceVariantLight,
    outline = NeoOutlineLight,
    outlineVariant = NeoOutlineLight,
    error = NeoError,
    onError = NeoOnError,
)

// ═══════════════════════════════════════════════════════════
// NEO GPT THEME
// ═══════════════════════════════════════════════════════════

enum class NeoThemeMode { Dark, Light, System, Amoled }

@Composable
fun NeoGPTTheme(
    themeMode: NeoThemeMode = NeoThemeMode.Dark,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = when (themeMode) {
        NeoThemeMode.Dark, NeoThemeMode.Amoled -> true
        NeoThemeMode.Light -> false
        NeoThemeMode.System -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == NeoThemeMode.Amoled -> NeoAmoledColorScheme
        isDark -> NeoDarkColorScheme
        else -> NeoLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NeoTypography,
        shapes = NeoMaterialShapes,
        content = content,
    )
}
