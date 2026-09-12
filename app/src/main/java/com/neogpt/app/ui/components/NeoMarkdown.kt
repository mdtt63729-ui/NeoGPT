package com.neogpt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoCodeStyle
import com.neogpt.app.ui.theme.NeoSpacing

/** Lightweight streaming Markdown renderer with styled inline emphasis/code. */
@Composable
fun NeoMarkdown(markdown: String, modifier: Modifier = Modifier, isStreaming: Boolean = false) {
    val lines = markdown.split("\n")
    Column(modifier.fillMaxWidth()) {
        lines.forEachIndexed { index, line ->
            NeoMarkdownLine(line, index == lines.lastIndex && isStreaming)
            if (index < lines.lastIndex) Spacer(Modifier.height(NeoSpacing.xs))
        }
    }
}

@Composable
private fun NeoMarkdownLine(line: String, isLastLine: Boolean) {
    // A restrained settle animation on the active streaming line. It restarts only
    // when new streamed text arrives, avoiding a distracting perpetual pulse.
    val reveal = remember { Animatable(1f) }
    LaunchedEffect(line, isLastLine) {
        if (isLastLine && line.isNotBlank()) {
            reveal.snapTo(0.92f)
            reveal.animateTo(1f, animationSpec = tween(180))
        } else {
            reveal.snapTo(1f)
        }
    }
    val activeModifier = Modifier.graphicsLayer {
        alpha = if (isLastLine) reveal.value else 1f
        translationY = if (isLastLine) (1f - reveal.value) * 2f else 0f
    }
    when {
        line.startsWith("# ") -> Text(inlineMarkdown(line.removePrefix("# ")), style = MaterialTheme.typography.headlineMedium, modifier = activeModifier)
        line.startsWith("## ") -> Text(inlineMarkdown(line.removePrefix("## ")), style = MaterialTheme.typography.headlineSmall, modifier = activeModifier)
        line.startsWith("### ") -> Text(inlineMarkdown(line.removePrefix("### ")), style = MaterialTheme.typography.titleLarge, modifier = activeModifier)
        line.startsWith("```") -> Text(line.removePrefix("```"), style = NeoCodeStyle, modifier = activeModifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(NeoSpacing.md))
        line.startsWith("> ") -> Text(inlineMarkdown(line.removePrefix("> ")), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = activeModifier.padding(start = NeoSpacing.lg))
        line.startsWith("- ") || line.startsWith("* ") -> Text(inlineMarkdown("•  ${line.removePrefix("- ").removePrefix("* ")}"), style = MaterialTheme.typography.bodyLarge, modifier = activeModifier.padding(start = NeoSpacing.md))
        line.matches(Regex("^\\d+\\. .*")) -> Text(inlineMarkdown(line), style = MaterialTheme.typography.bodyLarge, modifier = activeModifier.padding(start = NeoSpacing.md))
        line == "---" || line == "***" -> HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = NeoSpacing.sm))
        line.isBlank() -> Spacer(Modifier.height(NeoSpacing.xs))
        else -> Text(inlineMarkdown(line), style = MaterialTheme.typography.bodyLarge, modifier = activeModifier)
    }
}

@Composable
private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val token = Regex("(\\*\\*|__|`|(?<!\\*)\\*(?!\\*)|(?<!_)_(?!_))")
    var cursor = 0
    var bold = false
    var italic = false
    var code = false
    token.findAll(text).forEach { match ->
        if (match.range.first > cursor) {
            withStyle(SpanStyle(
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                background = if (code) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Unspecified,
                fontFamily = if (code) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
            )) { append(text.substring(cursor, match.range.first)) }
        }
        when (match.value) {
            "**", "__" -> bold = !bold
            "`" -> code = !code
            else -> italic = !italic
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        withStyle(SpanStyle(
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            background = if (code) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color.Unspecified,
            fontFamily = if (code) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default,
        )) { append(text.substring(cursor)) }
    }
}
