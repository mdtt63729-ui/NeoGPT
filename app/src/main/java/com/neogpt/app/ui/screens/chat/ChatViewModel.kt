package com.neogpt.app.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.domain.model.Message
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.ComposerMode
import com.neogpt.app.ui.components.MessageRole
import com.neogpt.app.ui.components.NeoMessageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

data class ChatUiState(
    val messages: List<NeoMessageData> = emptyList(),
    val isGenerating: Boolean = false,
    val modelName: String = "Gemini 2.5 Flash",
    val modelId: String = "gemini-2.5-flash",
    val activeMode: ComposerMode? = null,
    val error: String? = null,
)

class ChatViewModel(context: Context, modelId: String) : ViewModel() {
    private val appContext = context.applicationContext
    private val storage = SecureStorage(appContext)
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val source = GeminiDataSource(client, { storage.getApiKey().orEmpty() })
    private val _state = MutableStateFlow(
        ChatUiState(
            modelId = modelId,
            modelName = modelId.replace("-", " ").replaceFirstChar { it.uppercase() },
        )
    )
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    private var generationJob: Job? = null

    fun sendMessage(text: String) {
        if (text.isBlank() || _state.value.isGenerating) return
        val user = NeoMessageData(
            id = "${System.currentTimeMillis()}-u",
            role = MessageRole.USER,
            content = text.trim(),
        )
        val aiId = "${System.currentTimeMillis()}-a"
        val ai = NeoMessageData(aiId, MessageRole.AI, "", isStreaming = true)
        _state.update { it.copy(messages = it.messages + user + ai, isGenerating = true, error = null) }

        generationJob?.cancel()
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val key = storage.getApiKey().orEmpty()
                if (key.isBlank()) error("No Gemini API key. Open Settings and add one.")
                val messages = _state.value.messages
                    .filter { it.content.isNotBlank() }
                    .map {
                        Message(
                            id = it.id,
                            role = if (it.role == MessageRole.USER) Message.Role.USER else Message.Role.AI,
                            content = it.content,
                            timestamp = it.timestamp,
                        )
                    }
                val request = GeminiRequestMapper.buildRequest(messages, _state.value.modelId)
                var accumulated = ""
                source.streamGenerateContent(_state.value.modelId, request).collect { chunk ->
                    accumulated += chunk
                    _state.update { current ->
                        current.copy(messages = current.messages.map { m ->
                            if (m.id == aiId) m.copy(content = accumulated, isStreaming = true) else m
                        })
                    }
                }
                _state.update { current ->
                    current.copy(
                        isGenerating = false,
                        messages = current.messages.map { m -> if (m.id == aiId) m.copy(isStreaming = false) else m },
                    )
                }
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) return@launch
                _state.update { current ->
                    current.copy(
                        isGenerating = false,
                        error = t.message ?: "Unable to reach Gemini.",
                        messages = current.messages.filterNot { it.id == aiId },
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        _state.update { it.copy(isGenerating = false, messages = it.messages.map { m -> if (m.isStreaming) m.copy(isStreaming = false) else m }) }
    }

    fun copyMessage(messageId: String) {
        val text = _state.value.messages.firstOrNull { it.id == messageId }?.content ?: return
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Neo GPT", text))
    }

    fun shareMessage(messageId: String) {
        val text = _state.value.messages.firstOrNull { it.id == messageId }?.content ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(Intent.createChooser(intent, "Share response").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun regenerateMessage(messageId: String) {
        val index = _state.value.messages.indexOfFirst { it.id == messageId }
        if (index <= 0) return
        val prompt = _state.value.messages.take(index).lastOrNull { it.role == MessageRole.USER }?.content ?: return
        _state.update { it.copy(messages = it.messages.take(index)) }
        sendMessage(prompt)
    }

    fun editMessage(messageId: String) {
        // The screen can reuse the message as a new prompt; no destructive edit is performed.
    }

    fun likeMessage(messageId: String) {}
    fun dislikeMessage(messageId: String) {}

    override fun onCleared() {
        generationJob?.cancel()
        client.dispatcher.executorService.shutdown()
        super.onCleared()
    }
}
