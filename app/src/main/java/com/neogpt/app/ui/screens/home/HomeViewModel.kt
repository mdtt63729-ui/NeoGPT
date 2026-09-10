package com.neogpt.app.ui.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.neogpt.app.ui.components.NeoModelInfo

data class HomeUiState(
    val greeting: String = "What should we explore?",
    val selectedModel: NeoModelInfo = NeoModelInfo(
        id = "gemini-flash",
        name = "Gemini Flash",
        description = "Fast and versatile AI model",
        speedBadge = "Fast",
        contextWindow = "1M tokens",
        capabilities = listOf("text", "vision", "code"),
    ),
    val availableModels: List<NeoModelInfo> = listOf(
        NeoModelInfo(
            id = "gemini-flash",
            name = "Gemini Flash",
            description = "Fast and versatile AI model",
            speedBadge = "Fast",
            contextWindow = "1M tokens",
            capabilities = listOf("text", "vision", "code"),
        ),
        NeoModelInfo(
            id = "gemini-flash-lite",
            name = "Gemini Flash Lite",
            description = "Lightweight model for quick responses",
            speedBadge = "Fastest",
            contextWindow = "1M tokens",
            capabilities = listOf("text", "vision"),
        ),
        NeoModelInfo(
            id = "gemini-pro",
            name = "Gemini Pro",
            description = "Advanced model for complex tasks",
            speedBadge = "Balanced",
            contextWindow = "2M tokens",
            capabilities = listOf("text", "vision", "code", "tools"),
        ),
    ),
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val greetings = listOf(
        "What should we explore?",
        "What can we create today?",
        "Ready when you are.",
        "Any new ideas to explore?",
        "Where should we begin?",
        "Let's make progress.",
        "How can I help you today?",
        "Evening, how are things?",
    )

    init {
        // Random greeting on init
        _state.update { it.copy(greeting = greetings.random()) }
    }

    fun selectModel(model: NeoModelInfo) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun sendMessage(text: String) {
        // Navigate to chat — handled by the screen
    }
}
