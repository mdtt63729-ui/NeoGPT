package com.neogpt.app.coding

/**
 * Small, dependency-free line patch engine used by the coding workspace.
 * It supports the unified-diff form produced by [createPatch].
 */
class PatchManager {
    fun applyPatch(original: String, patch: String): String {
        val source = original.split("\n", ignoreCase = false, limit = -1).toMutableList()
        val lines = patch.split("\n", ignoreCase = false, limit = -1)
        val result = mutableListOf<String>()
        var sourceIndex = 0

        for (line in lines) {
            when {
                line.startsWith("@@") -> {
                    val match = Regex("@@ -(\\d+)(?:,(\\d+))? \\+(\\d+)(?:,(\\d+))? @@").find(line)
                        ?: throw IllegalArgumentException("Invalid unified diff hunk: $line")
                    val target = match.groupValues[1].toInt() - 1
                    while (sourceIndex < target && sourceIndex < source.size) {
                        result += source[sourceIndex++]
                    }
                }
                line.startsWith("--- ") || line.startsWith("+++ ") -> Unit
                line.startsWith(" ") -> {
                    val expected = line.drop(1)
                    require(sourceIndex < source.size && source[sourceIndex] == expected) {
                        "Patch context does not match the original content."
                    }
                    result += source[sourceIndex++]
                }
                line.startsWith("-") -> {
                    val expected = line.drop(1)
                    require(sourceIndex < source.size && source[sourceIndex] == expected) {
                        "Patch deletion does not match the original content."
                    }
                    sourceIndex++
                }
                line.startsWith("+") -> result += line.drop(1)
                line == "\\ No newline at end of file" -> Unit
                else -> throw IllegalArgumentException("Unsupported patch line: $line")
            }
        }

        while (sourceIndex < source.size) result += source[sourceIndex++]
        return result.joinToString("\n")
    }

    fun createPatch(original: String, modified: String): String {
        if (original == modified) return "--- original\n+++ modified\n"

        val oldLines = original.split("\n", limit = -1)
        val newLines = modified.split("\n", limit = -1)
        val prefix = oldLines.zip(newLines).takeWhile { it.first == it.second }.size
        val suffix = oldLines.asReversed().zip(newLines.asReversed())
            .takeWhile { it.first == it.second }
            .size
            .coerceAtMost(minOf(oldLines.size - prefix, newLines.size - prefix))

        val oldEnd = oldLines.size - suffix
        val newEnd = newLines.size - suffix
        val removed = oldLines.subList(prefix, oldEnd)
        val added = newLines.subList(prefix, newEnd)

        val oldStart = prefix + 1
        val newStart = prefix + 1
        val oldCount = removed.size
        val newCount = added.size

        return buildString {
            appendLine("--- original")
            appendLine("+++ modified")
            appendLine("@@ -$oldStart,$oldCount +$newStart,$newCount @@")
            removed.forEach { appendLine("-$it") }
            added.forEach { appendLine("+$it") }
            oldLines.takeLast(suffix).forEach { appendLine(" $it") }
        }.trimEnd('\n')
    }
}
