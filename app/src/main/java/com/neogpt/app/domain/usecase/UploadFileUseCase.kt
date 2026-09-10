package com.neogpt.app.domain.usecase

import com.neogpt.app.data.repository.FileRepository

class UploadFileUseCase(
    private val fileRepository: FileRepository,
) {
    suspend operator fun invoke(
        name: String,
        mimeType: String,
        path: String,
        sizeBytes: Long,
    ) {
        fileRepository.addFile(name, mimeType, path, sizeBytes)
    }
}
