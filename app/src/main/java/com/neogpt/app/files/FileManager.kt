package com.neogpt.app.files

import android.content.Context
import java.io.File

class FileManager(context: Context) {
    private val baseDir = context.filesDir

    fun getProjectDir(projectId: String): File {
        return File(baseDir, "projects/$projectId").apply { mkdirs() }
    }

    fun saveFile(projectId: String, name: String, content: ByteArray): File {
        val dir = getProjectDir(projectId)
        val file = File(dir, name)
        file.writeBytes(content)
        return file
    }

    fun listFiles(projectId: String): List<File> {
        return getProjectDir(projectId).listFiles()?.toList() ?: emptyList()
    }

    fun deleteFile(file: File): Boolean = file.delete()
}
