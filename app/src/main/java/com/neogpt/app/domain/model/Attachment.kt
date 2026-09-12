package com.neogpt.app.domain.model

data class Attachment(
    val id: String,
    val name: String,
    val type: Type,
    val mimeType: String,
    val uri: String? = null,
    val base64Data: String? = null,
    val sizeBytes: Long = 0,
) {
    enum class Type { IMAGE, FILE, AUDIO, VIDEO }
}
