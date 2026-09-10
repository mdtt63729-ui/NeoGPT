package com.neogpt.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.neogpt.app.ui.components.ComposerMode
import com.neogpt.app.ui.components.MessageRole
import com.neogpt.app.ui.components.NeoMessageData

data class ChatUiState(
    val messages: List<NeoMessageData> = emptyList(),
    val isGenerating: Boolean = false,
    val modelName: String = "Gemini Flash",
    val activeMode: ComposerMode? = null,
    val error: String? = null,
)

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    fun sendMessage(text: String) {
        val userMessage = NeoMessageData(
            id = System.currentTimeMillis().toString(),
            role = MessageRole.USER,
            content = text,
        )
        val aiMessageId = (System.currentTimeMillis() + 1).toString()
        val aiPlaceholder = NeoMessageData(
            id = aiMessageId,
            role = MessageRole.AI,
            content = "",
            isStreaming = true,
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage + aiPlaceholder,
                isGenerating = true,
            )
        }

        // TODO: Call Gemini API repository to stream response
        // For now, this is the architecture — actual API call
        // would be: geminiRepository.streamResponse(text, modelId)
        // and chunks would update the AI message content
    }

    fun stopGeneration() {
        _state.update {
            it.copy(
                isGenerating = false,
                messages = it.messages.map { msg ->
                    if (msg.isStreaming) msg.copy(isStreaming = false) else msg
                },
            )
        }
        // TODO: Cancel Gemini API streaming request
    }

    fun copyMessage(messageId: String) { /* TODO */ }
    fun regenerateMessage(messageId: String) { /* TODO */ }
    fun shareMessage(messageId: String) { /* TODO */ }
    fun editMessage(messageId: String) { /* TODO */ }
    fun likeMessage(messageId: String) { /* TODO */ }
    fun dislikeMessage(messageId: String) { /* TODO */ }
}
