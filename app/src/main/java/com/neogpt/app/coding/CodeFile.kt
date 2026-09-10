package com.neogpt.app.coding

import java.io.File

data class CodeFile(
    val path: String,
    val name: String,
    val content: String = "",
    val language: String = "",
) {
    companion object {
        fun fromFile(file: File): CodeFile {
            val ext = file.extension.lowercase()
            val lang = when (ext) {
                "kt" -> "kotlin"; "java" -> "java"; "py" -> "python"
                "js" -> "javascript"; "ts" -> "typescript"; "go" -> "go"
                "xml" -> "xml"; "json" -> "json"; else -> "text"
            }
            return CodeFile(
                path = file.absolutePath,
                name = file.name,
                content = if (file.canRead()) file.readText() else "",
                language = lang,
            )
        }
    }
}
