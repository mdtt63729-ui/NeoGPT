package com.neogpt.app.files

import android.webkit.MimeTypeMap
import java.io.File

object FileTypeDetector {
    fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }

    fun isImage(file: File): Boolean = getMimeType(file).startsWith("image/")
    fun isPdf(file: File): Boolean = file.extension.lowercase() == "pdf"
    fun isCode(file: File): Boolean = file.extension.lowercase() in
        listOf("kt", "java", "py", "js", "ts", "json", "xml", "html", "css", "go", "rs", "cpp", "c", "h")
    fun isAudio(file: File): Boolean = getMimeType(file).startsWith("audio/")
    fun isVideo(file: File): Boolean = getMimeType(file).startsWith("video/")
}
