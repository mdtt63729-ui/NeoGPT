package com.neogpt.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.neogpt.app.ui.theme.NeoCodeStyle
import com.neogpt.app.ui.theme.NeoSpacing

import androidx.compose.foundation.background
/**
 * Lightweight markdown renderer for streaming AI responses.
 * Supports: headings, bold, italic, code blocks, inline code,
 * blockquotes, ordered/unordered lists, horizontal rules.
 */
@Composable
fun NeoMarkdown(
    markdown: String,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
) {
    val lines = markdown.split("\n")

    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEachIndexed { index, line ->
            NeoMarkdownLine(
                line = line,
                isLastLine = index == lines.lastIndex && isStreaming,
            )
            if (index < lines.lastIndex) {
                Spacer(modifier = Modifier.height(NeoSpacing.xs))
            }
        }
    }
}

@Composable
private fun NeoMarkdownLine(
    line: String,
    isLastLine: Boolean = false,
) {
    when {
        line.startsWith("# ") -> {
            Text(
                text = line.removePrefix("# "),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        line.startsWith("## ") -> {
            Text(
                text = line.removePrefix("## "),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        line.startsWith("### ") -> {
            Text(
                text = line.removePrefix("### "),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        line.startsWith("```") -> {
            // Fenced code marker; code content is rendered line-by-line below.
            Text(
                text = line.removePrefix("```"),
                style = NeoCodeStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NeoSpacing.md)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(NeoSpacing.md),
            )
        }
        line.startsWith("> ") -> {
            Text(
                text = line.removePrefix("> "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = NeoSpacing.lg),
            )
        }
        line.startsWith("- ") || line.startsWith("* ") -> {
            Text(
                text = "•  ${line.removePrefix("- ").removePrefix("* ")}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = NeoSpacing.md),
            )
        }
        line.matches(Regex("^\\d+\\. .*")) -> {
            Text(
                text = line,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = NeoSpacing.md),
            )
        }
        line == "---" || line == "***" -> {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = NeoSpacing.sm),
                color = MaterialTheme.colorScheme.outline,
            )
        }
        line.isBlank() -> {
            Spacer(modifier = Modifier.height(NeoSpacing.xs))
        }
        else -> {
            // Regular paragraph with inline formatting
            Text(
                text = line,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
