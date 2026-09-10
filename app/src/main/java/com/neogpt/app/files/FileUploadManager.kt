package com.neogpt.app.files

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

class FileUploadManager(context: Context) {
    private val fileManager = FileManager(context)

    fun uriToBase64(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ""
        val bytes = inputStream.readBytes()
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun getFileInfo(uri: Uri): Pair<String, Long>? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (it.moveToFirst() && nameIndex >= 0) {
                val name = it.getString(nameIndex)
                val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                return name to size
            }
        }
        return null
    }

    private val context = context
}
