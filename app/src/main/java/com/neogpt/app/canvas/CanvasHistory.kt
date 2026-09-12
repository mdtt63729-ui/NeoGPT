package com.neogpt.app.canvas

class CanvasHistory {
    private val history = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var currentIndex = -1

    fun save(content: String) {
        if (currentIndex < history.size - 1) {
            history.subList(currentIndex + 1, history.size).clear()
        }
        history.add(content)
        currentIndex = history.size - 1
    }

    fun undo(): String? {
        if (currentIndex > 0) {
            currentIndex--
            redoStack.clear()
            return history[currentIndex]
        }
        return null
    }

    fun redo(): String? {
        if (currentIndex < history.size - 1) {
            currentIndex++
            return history[currentIndex]
        }
        return null
    }
}
