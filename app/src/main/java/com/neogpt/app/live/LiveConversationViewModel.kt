package com.neogpt.app.live

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.security.SecureStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

private const val USER_SPEECH_INTERRUPT_LEVEL = 0.13f

data class LiveConversationUiState(
    val connection: LiveConnectionState = LiveConnectionState.IDLE,
    val status: String = "Ready to talk",
    val userTranscript: String = "",
    val assistantTranscript: String = "",
    val userLevel: Float = 0f,
    val assistantLevel: Float = 0f,
    val error: String? = null,
) {
    val active: Boolean get() = connection == LiveConnectionState.CONNECTING || connection == LiveConnectionState.CONNECTED
}

class LiveConversationViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val storage = SecureStorage(appContext)
    private val _state = MutableStateFlow(LiveConversationUiState())
    val state: StateFlow<LiveConversationUiState> = _state.asStateFlow()

    private val repository = GeminiLiveRepository(storage.getProviderKey(AiProvider.GEMINI.id).orEmpty())
    private val audioInput = LiveAudioInput()
    private val audioOutput = LiveAudioOutput(viewModelScope)
    private var inputJob: Job? = null
    private var eventJob: Job? = null
    private var started = false

    init {
        eventJob = viewModelScope.launch {
            launch {
                repository.connection.collectLatest { connection ->
                    _state.update { current ->
                        current.copy(
                            connection = connection,
                            status = when (connection) {
                                LiveConnectionState.IDLE -> "Ready to talk"
                                LiveConnectionState.CONNECTING -> "Connecting to Gemini Live…"
                                LiveConnectionState.CONNECTED -> "Listening"
                                LiveConnectionState.ERROR -> "Connection problem"
                            },
                            error = if (connection == LiveConnectionState.ERROR) current.error else null,
                        )
                    }
                }
            }
            launch {
                repository.assistantAudio.collect { pcm ->
                    audioOutput.enqueue(pcm)
                    val level = calculateRms(pcm)
                    _state.update { it.copy(assistantLevel = level, status = "Speaking") }
                }
            }
            launch {
                repository.events.collect { event ->
                    when (event) {
                        GeminiLiveEvent.Ready -> {
                            audioOutput.start()
                            _state.update { it.copy(status = "Listening", error = null) }
                        }
                        GeminiLiveEvent.Interrupted -> {
                            audioOutput.interrupt()
                            _state.update { it.copy(status = "Listening", assistantLevel = 0f) }
                        }
                        GeminiLiveEvent.AssistantAudioStarted -> {
                            _state.update { it.copy(status = "Speaking") }
                        }
                        GeminiLiveEvent.TurnComplete -> {
                            _state.update { it.copy(status = "Listening", assistantLevel = 0f) }
                        }
                        is GeminiLiveEvent.UserTranscript -> {
                            _state.update { current ->
                                current.copy(userTranscript = event.text, status = if (event.interim) "Listening" else "Thinking")
                            }
                        }
                        is GeminiLiveEvent.AssistantTranscript -> {
                            _state.update { it.copy(assistantTranscript = event.text, status = "Speaking") }
                        }
                        is GeminiLiveEvent.Status -> {
                            if (_state.value.connection != LiveConnectionState.ERROR) {
                                _state.update { it.copy(status = event.text) }
                            }
                        }
                        is GeminiLiveEvent.Error -> {
                            started = false
                            inputJob?.cancel()
                            inputJob = null
                            audioInput.release()
                            audioOutput.interrupt()
                            _state.update { it.copy(error = event.message, status = "Connection problem") }
                        }
                    }
                }
            }
        }
    }

    fun start() {
        if (started) return
        val key = storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()
        if (key.isBlank()) {
            _state.update { it.copy(connection = LiveConnectionState.ERROR, status = "Gemini key required", error = "Gemini API key is not configured. Open Settings → API & Secrets and add your Gemini key.") }
            return
        }

        started = true
        _state.value = LiveConversationUiState(connection = LiveConnectionState.CONNECTING, status = "Connecting to Gemini Live…")
        repository.connect()
        inputJob?.cancel()
        inputJob = viewModelScope.launch {
            try {
                audioInput.stream().collect { pcm ->
                    val level = calculateRms(pcm)
                    _state.update { it.copy(userLevel = level) }

                    // Local barge-in makes the app feel immediate even before the
                    // server's interrupted event reaches the phone.
                    if (_state.value.status == "Speaking" && level >= USER_SPEECH_INTERRUPT_LEVEL) {
                        audioOutput.interrupt()
                    }
                    repository.sendAudio(pcm)
                }
            } catch (error: SecurityException) {
                _state.update { it.copy(connection = LiveConnectionState.ERROR, status = "Microphone permission required", error = "Microphone permission is required for Live Conversation.") }
            } catch (error: Throwable) {
                if (started) {
                    _state.update { it.copy(connection = LiveConnectionState.ERROR, status = "Microphone unavailable", error = error.message ?: "Could not start the microphone.") }
                }
            }
        }
    }

    fun stop() {
        started = false
        inputJob?.cancel()
        inputJob = null
        audioInput.release()
        audioOutput.interrupt()
        repository.disconnect()
        _state.value = LiveConversationUiState()
    }

    private fun calculateRms(pcm: ByteArray): Float {
        if (pcm.size < 2) return 0f
        var sum = 0.0
        var count = 0
        var index = 0
        while (index + 1 < pcm.size) {
            val sample = ((pcm[index + 1].toInt() shl 8) or (pcm[index].toInt() and 0xff)).toShort().toInt()
            sum += sample.toDouble() * sample.toDouble()
            count++
            index += 2
        }
        return if (count == 0) 0f else (sqrt(sum / count) / 32768.0).toFloat().coerceIn(0f, 1f)
    }

    override fun onCleared() {
        stop()
        eventJob?.cancel()
        audioOutput.release()
        super.onCleared()
    }
}
