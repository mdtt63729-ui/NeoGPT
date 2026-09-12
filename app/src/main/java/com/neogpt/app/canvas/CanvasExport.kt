package com.neogpt.app.canvas

import java.io.File

class CanvasExport {
    fun exportToMarkdown(doc: CanvasDocument, outputDir: File): File {
        val file = File(outputDir, "${doc.title}.md")
        file.writeText(doc.content)
        return file
    }

    fun exportToText(doc: CanvasDocument, outputDir: File): File {
        val file = File(outputDir, "${doc.title}.txt")
        file.writeText(doc.content)
        return file
    }
}
