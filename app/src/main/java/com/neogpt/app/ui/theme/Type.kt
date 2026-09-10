package com.neogpt.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.neogpt.app.R

// ═══════════════════════════════════════════════════════════
// JOSEFIN SANS — Global App Font
// ═══════════════════════════════════════════════════════════

val NeoFontFamily = FontFamily(
    Font(R.font.josefin_sans_thin, FontWeight.Thin, FontStyle.Normal),
    Font(R.font.josefin_sans_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.josefin_sans_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.josefin_sans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.josefin_sans_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.josefin_sans_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.josefin_sans_bold, FontWeight.Bold, FontStyle.Normal),
)

// Josefin Sans is the single default typeface for the entire app.
// Even code/technical text intentionally uses Josefin Sans to keep the UI consistent.
val NeoMonoFamily = NeoFontFamily

// ═══════════════════════════════════════════════════════════
// NEO GPT TYPOGRAPHY SCALE
// Material 3 base + Josefin Sans
// ═══════════════════════════════════════════════════════════

val NeoTypography = Typography(
    // Display — Splash logo, large greetings
    displayLarge = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp,
    ),

    // Headline — Page titles, section headers
    headlineLarge = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),

    // Title — Top bar, list items, cards
    titleLarge = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body — Primary text, messages, paragraphs
    bodyLarge = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label — Buttons, chips, badges, timestamps
    labelLarge = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = NeoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ═══════════════════════════════════════════════════════════
// CODE TEXT STYLE
// ═══════════════════════════════════════════════════════════

val NeoCodeStyle = TextStyle(
    fontFamily = NeoMonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
)
