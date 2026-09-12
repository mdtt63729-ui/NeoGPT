package com.neogpt.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.NeoModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val greeting: String = "What can I help you with?",
    val selectedModel: NeoModelInfo = defaultModels.first(),
    val availableModels: List<NeoModelInfo> = defaultModels,
    val modelsLoading: Boolean = false,
) {
    companion object {
        val defaultModels = listOf(
            NeoModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", "Fast, capable everyday model", "Fast", "1M", listOf("text", "vision", "code")),
            NeoModelInfo("gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite", "Fastest and most economical 2.5 model", "Fastest", "1M", listOf("text", "vision")),
            NeoModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro", "Advanced reasoning and coding", "Advanced", "1M", listOf("text", "vision", "code")),
        )
    }
}

class HomeViewModel(context: Context) : ViewModel() {
    private val storage = SecureStorage(context.applicationContext)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    private val source = GeminiDataSource(client, { storage.getApiKey().orEmpty() })
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val greetings = listOf(
        "What can I help you with?",
        "What should we work on?",
        "Ready when you are.",
        "What are you thinking about?",
        "Where should we begin?",
    )

    init {
        _state.update { it.copy(greeting = greetings.random(), modelsLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { source.listModels() }
                .onSuccess { remote ->
                    val models = remote
                        .asSequence()
                        .filter { it.supportsStreamingTextGeneration && it.id.startsWith("gemini-") }
                        .filterNot { it.id.contains("tts") || it.id.contains("live") || it.id.contains("image") }
                        .mapNotNull { model ->
                            val id = model.id
                            val display = model.displayName?.takeIf { it.isNotBlank() } ?: prettyName(id)
                            val context = model.inputTokenLimit?.let { "${it / 1_000_000}M" } ?: "—"
                            NeoModelInfo(
                                id = id,
                                name = display,
                                description = model.description?.lineSequence()?.firstOrNull().orEmpty().ifBlank { "Gemini model" },
                                speedBadge = if (id.contains("flash-lite")) "Fastest" else if (id.contains("flash")) "Fast" else "Advanced",
                                contextWindow = context,
                                capabilities = listOf("text"),
                            )
                        }
                        .distinctBy { it.id }
                        .sortedWith(compareBy<NeoModelInfo> { priority(it.id) }.thenBy { it.name })
                        .take(8)
                        .toList()
                    if (models.isNotEmpty()) {
                        _state.update { current ->
                            val selected = models.firstOrNull { it.id == current.selectedModel.id } ?: models.first()
                            current.copy(availableModels = models, selectedModel = selected, modelsLoading = false)
                        }
                    } else {
                        _state.update { it.copy(modelsLoading = false) }
                    }
                }
                .onFailure { _state.update { it.copy(modelsLoading = false) } }
        }
    }

    fun selectModel(model: NeoModelInfo) {
        _state.update { it.copy(selectedModel = model) }
    }

    private fun priority(id: String): Int = when {
        id == "gemini-2.5-flash" -> 0
        id == "gemini-2.5-flash-lite" -> 1
        id == "gemini-2.5-pro" -> 2
        id.contains("3.7-flash") -> 3
        id.contains("3.6-flash") -> 4
        else -> 10
    }

    private fun prettyName(id: String): String = id.removePrefix("gemini-")
        .split('-')
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercase() } }

    override fun onCleared() {
        client.dispatcher.executorService.shutdown()
        super.onCleared()
    }
}
