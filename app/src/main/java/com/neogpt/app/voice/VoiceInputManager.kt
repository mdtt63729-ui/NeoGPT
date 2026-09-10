package com.neogpt.app.voice

import android.content.Context
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceInputManager(context: Context) {
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    fun startListening() {
        _state.value = VoiceState.LISTENING
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        }
        recognizer.startListening(intent)
    }

    fun stopListening() {
        _state.value = VoiceState.IDLE
        recognizer.stopListening()
    }

    fun destroy() {
        recognizer.destroy()
    }
}
