package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.MemoryRepository

class ManageMemoryUseCase(
    private val memoryRepository: MemoryRepository,
) {
    suspend fun add(content: String) = memoryRepository.addMemory(content)
    suspend fun clearAll() = memoryRepository.clearAllMemories()
}
