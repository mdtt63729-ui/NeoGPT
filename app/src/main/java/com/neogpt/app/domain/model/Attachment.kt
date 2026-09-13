package com.neogpt.app.domain.model

data class Attachment(
    val id: String,
    val name: String,
    val type: Type,
    val mimeType: String,
    /** Provider URI, such as a Gemini File API URI. */
    val uri: String? = null,
    /** Original local content:// URI so the user can reopen the attachment. */
    val localUri: String? = null,
    val base64Data: String? = null,
    val sizeBytes: Long = 0,
) {
    enum class Type { IMAGE, FILE, AUDIO, VIDEO }
}
