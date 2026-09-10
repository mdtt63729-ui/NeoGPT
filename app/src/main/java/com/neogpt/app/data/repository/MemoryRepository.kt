package com.neogpt.app.data.repository

import com.neogpt.app.data.local.dao.MemoryDao
import com.neogpt.app.data.local.entity.MemoryEntity
import com.neogpt.app.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MemoryRepository(private val memoryDao: MemoryDao) {
    fun getAllMemories(): Flow<List<String>> =
        memoryDao.getAllMemories().map { entities ->
            entities.map { it.content }
        }

    suspend fun addMemory(content: String) {
        memoryDao.insertMemory(
            MemoryEntity(
                id = System.currentTimeMillis().toString(),
                content = content,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun clearAllMemories() {
        memoryDao.clearAllMemories()
    }
}
