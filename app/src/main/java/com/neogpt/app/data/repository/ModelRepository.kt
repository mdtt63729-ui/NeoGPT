package com.neogpt.app.data.repository

import com.neogpt.app.domain.model.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModelRepository {
    private val _models = MutableStateFlow(
        listOf(
            ModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Fast and capable", "Fast", "1M", listOf("text", "vision", "code")),
            ModelInfo("gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite", "Fastest and economical", "Fastest", "1M", listOf("text", "vision")),
            ModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "Advanced reasoning and coding", "Advanced", "1M", listOf("text", "vision", "code", "tools")),
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
