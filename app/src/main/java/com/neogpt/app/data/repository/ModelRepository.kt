package com.neogpt.app.data.repository

import com.neogpt.app.domain.model.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModelRepository {
    private val _models = MutableStateFlow(
        listOf(
            ModelInfo("gemini-flash", "Gemini Flash", "Fast and versatile", "Fast", "1M", listOf("text", "vision", "code")),
            ModelInfo("gemini-flash-lite", "Gemini Flash Lite", "Lightweight", "Fastest", "1M", listOf("text", "vision")),
            ModelInfo("gemini-pro", "Gemini Pro", "Advanced model", "Balanced", "2M", listOf("text", "vision", "code", "tools")),
        )
    )
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow(_models.value.first())
    val selectedModel: StateFlow<ModelInfo> = _selectedModel.asStateFlow()

    fun selectModel(model: ModelInfo) {
        _selectedModel.value = model
    }

    fun updateModels(models: List<ModelInfo>) {
        _models.value = models
    }
}
