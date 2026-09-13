package com.neogpt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.neogpt.app.ui.theme.NeoCodeStyle
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoSpacing

@Composable
fun NeoMarkdown(markdown: String, modifier: Modifier = Modifier, isStreaming: Boolean = false, textScale: Float = 1f) {
    val lines = markdown.split("\n")
    Column(modifier.fillMaxWidth()) {
        lines.forEachIndexed { index, line ->
            NeoMarkdownLine(line, textScale.coerceIn(0.80f, 1.40f))
            if (index < lines.lastIndex) Spacer(Modifier.height(NeoSpacing.xs))
        }
    }
}

private fun TextStyle.scaled(scale: Float) = copy(
    fontSize = if (fontSize.isSp) fontSize * scale else fontSize,
    lineHeight = if (lineHeight.isSp) lineHeight * scale else lineHeight,
)

@Composable
private fun NeoMarkdownLine(line: String, textScale: Float) {
    val body = MaterialTheme.typography.bodyLarge.scaled(textScale)
    when {
        line.startsWith("# ") -> Text(inlineMarkdown(line.removePrefix("# ")), style = MaterialTheme.typography.headlineMedium.scaled(textScale))
        line.startsWith("## ") -> Text(inlineMarkdown(line.removePrefix("## ")), style = MaterialTheme.typography.headlineSmall.scaled(textScale))
        line.startsWith("### ") -> Text(inlineMarkdown(line.removePrefix("### ")), style = MaterialTheme.typography.titleLarge.scaled(textScale))
        line.startsWith("```") -> Text(
            line.removePrefix("```").ifBlank { " " },
            style = NeoCodeStyle.scaled(textScale),
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(NeoSpacing.md),
        )
        line.startsWith("> ") -> Text(inlineMarkdown(line.removePrefix("> ")), style = body, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = NeoSpacing.lg))
        line.startsWith("- ") || line.startsWith("* ") -> Text(inlineMarkdown("•  ${line.removePrefix("- ").removePrefix("* ")}"), style = body, modifier = Modifier.padding(start = NeoSpacing.md))
        line.matches(Regex("^\\d+\\. .*")) -> Text(inlineMarkdown(line), style = body, modifier = Modifier.padding(start = NeoSpacing.md))
        line == "---" || line == "***" -> HorizontalDivider(Modifier.fillMaxWidth().padding(vertical = NeoSpacing.sm))
        line.isBlank() -> Spacer(Modifier.height(NeoSpacing.xs))
        else -> Text(inlineMarkdown(line), style = body)
    }
}

@Composable
private fun inlineMarkdown(text: String): AnnotatedString {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    return buildAnnotatedString {
        val token = Regex("""(\\*\\*|__|~~|`|\\+\\+|<u>|</u>|(?<!\\*)\\*(?!\\*)|(?<!_)_(?!_))""")
        var cursor = 0
        var bold = false
        var italic = false
        var code = false
        var underline = false
        var strike = false

        fun currentStyle(): SpanStyle = SpanStyle(
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = when {
                underline && strike -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                underline -> TextDecoration.Underline
                strike -> TextDecoration.LineThrough
                else -> TextDecoration.None
            },
            background = if (code) surfaceVariant else Color.Unspecified,
            fontFamily = if (code) FontFamily.Monospace else NeoFontFamily,
        )

        token.findAll(text).forEach { match ->
            if (match.range.first > cursor) pushStyle(currentStyle()); append(text.substring(cursor, match.range.first)); pop()
            when (match.value) {
                "**", "__" -> bold = !bold
                "`" -> code = !code
                "~~" -> strike = !strike
                "++", "<u>", "</u>" -> underline = !underline
                else -> italic = !italic
            }
            cursor = match.range.last + 1
        }
        if (cursor < text.length) pushStyle(currentStyle()); append(text.substring(cursor)); pop()
    }
}
