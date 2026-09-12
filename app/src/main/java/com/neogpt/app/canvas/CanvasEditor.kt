package com.neogpt.app.canvas

class CanvasEditor {
    private var document: CanvasDocument? = null

    fun load(doc: CanvasDocument) {
        document = doc
    }

    fun edit(newContent: String): CanvasDocument {
        val doc = document ?: throw IllegalStateException("No document loaded")
        val updated = doc.copy(content = newContent, updatedAt = System.currentTimeMillis())
        document = updated
        return updated
    }

    fun getCurrent(): CanvasDocument? = document
}
