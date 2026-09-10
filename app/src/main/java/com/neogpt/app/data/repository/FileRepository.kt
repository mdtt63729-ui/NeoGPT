package com.neogpt.app.data.repository

import com.neogpt.app.data.local.dao.FileDao
import com.neogpt.app.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FileRepository(private val fileDao: FileDao) {
    fun getAllFiles(): Flow<List<FileEntity>> = fileDao.getAllFiles()

    suspend fun addFile(name: String, mimeType: String, path: String, sizeBytes: Long) {
        fileDao.insertFile(
            FileEntity(
                id = System.currentTimeMillis().toString(),
                name = name,
                mimeType = mimeType,
                path = path,
                sizeBytes = sizeBytes,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
