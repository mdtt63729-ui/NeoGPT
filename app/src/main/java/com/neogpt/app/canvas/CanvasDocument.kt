package com.neogpt.app.canvas

data class CanvasDocument(
    val id: String,
    val title: String = "Untitled",
    val content: String = "",
    val type: CanvasType = CanvasType.DOCUMENT,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class CanvasType { DOCUMENT, CODE, TABLE, STRUCTURED }
