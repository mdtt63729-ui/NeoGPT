package com.neogpt.app.files

import java.io.File

object FilePreview {
    fun generatePreview(file: File, maxLines: Int = 50): String {
        if (!file.exists() || !file.canRead()) return "Unable to read file"
        return file.useLines { it.take(maxLines).joinToString("\n") }
    }
}
