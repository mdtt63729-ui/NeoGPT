package com.neogpt.app.coding

data class DiffLine(
    val type: DiffType,
    val content: String,
)

enum class DiffType { ADDED, REMOVED, UNCHANGED }

class DiffEngine {
    fun computeDiff(old: String, new: String): List<DiffLine> {
        val oldLines = old.split("\n")
        val newLines = new.split("\n")
        val result = mutableListOf<DiffLine>()

        val maxLines = maxOf(oldLines.size, newLines.size)
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i) ?: ""
            val newLine = newLines.getOrNull(i) ?: ""
            when {
                oldLine == newLine -> result.add(DiffLine(DiffType.UNCHANGED, newLine))
                oldLine.isEmpty() && newLine.isNotEmpty() -> result.add(DiffLine(DiffType.ADDED, newLine))
                oldLine.isNotEmpty() && newLine.isEmpty() -> result.add(DiffLine(DiffType.REMOVED, oldLine))
                else -> {
                    result.add(DiffLine(DiffType.REMOVED, oldLine))
                    result.add(DiffLine(DiffType.ADDED, newLine))
                }
            }
        }
        return result
    }
}
