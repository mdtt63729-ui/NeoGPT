package com.neogpt.app.files

import android.net.Uri

data class FilePickResult(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
)
