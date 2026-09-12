package com.neogpt.app.data.repository

import com.neogpt.app.domain.model.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModelRepository {
    private val _models = MutableStateFlow(
        listOf(
            ModelInfo("gemini-3.8-flash", "Gemini 3.8 Flash", "Latest stable Flash", "Fast+", "1M", listOf("text", "vision", "thinking")),
            ModelInfo("gemini-3.7-flash", "Gemini 3.7 Flash", "Stable reasoning Flash", "Fast", "1M", listOf("text", "vision", "thinking")),
            ModelInfo("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "Fastest cost-efficient Flash", "Fastest", "1M", listOf("text", "vision", "thinking")),
            ModelInfo("gemini-3.6-flash", "Gemini 3.6 Flash", "Balanced speed and multimodality", "Fast", "1M", listOf("text", "vision", "thinking")),
            ModelInfo("gemini-3.5-flash", "Gemini 3.5 Flash", "High-capability stable Flash", "Fast", "1M", listOf("text", "vision", "thinking")),
        )
    )
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()
    private val _selectedModel = MutableStateFlow(_models.value.first())
    val selectedModel: StateFlow<ModelInfo> = _selectedModel.asStateFlow()
    fun selectModel(model: ModelInfo) { _selectedModel.value = model }
    fun updateModels(models: List<ModelInfo>) { _models.value = models }
}
