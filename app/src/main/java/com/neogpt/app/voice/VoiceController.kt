package com.neogpt.app.voice

class VoiceController(
    private val inputManager: VoiceInputManager,
) {
    fun startVoiceMode() {
        inputManager.startListening()
    }

    fun stopVoiceMode() {
        inputManager.stopListening()
    }
}
