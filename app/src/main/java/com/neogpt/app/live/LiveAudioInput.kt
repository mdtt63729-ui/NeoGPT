package com.neogpt.app.live

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/** 16 kHz mono PCM16 microphone stream for Gemini Live. */
class LiveAudioInput(
    private val sampleRate: Int = 16_000,
) {
    private var recorder: AudioRecord? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null

    @SuppressLint("MissingPermission")
    fun stream(): Flow<ByteArray> = flow {
        val channel = AudioFormat.CHANNEL_IN_MONO
        val format = AudioFormat.ENCODING_PCM_16BIT
        val minimum = AudioRecord.getMinBufferSize(sampleRate, channel, format)
        val bufferSize = (minimum.coerceAtLeast(2048) * 2).coerceAtLeast(4096)

        val local = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            channel,
            format,
            bufferSize,
        )
        recorder = local

        try {
            check(local.state == AudioRecord.STATE_INITIALIZED) { "Microphone could not be initialized." }
            val sessionId = local.audioSessionId
            if (AcousticEchoCanceler.isAvailable() && sessionId != 0) {
                echoCanceler = AcousticEchoCanceler.create(sessionId)?.also { it.enabled = true }
            }
            if (NoiseSuppressor.isAvailable() && sessionId != 0) {
                noiseSuppressor = NoiseSuppressor.create(sessionId)?.also { it.enabled = true }
            }

            local.startRecording()
            val chunk = ByteArray(2048) // ~64 ms at 16 kHz mono PCM16.
            while (coroutineContext.isActive) {
                val read = local.read(chunk, 0, chunk.size, AudioRecord.READ_BLOCKING)
                if (read > 0) emit(chunk.copyOf(read)) else if (read < 0) break
            }
        } finally {
            release()
        }
    }.flowOn(Dispatchers.IO)

    fun release() {
        try { recorder?.stop() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        try { echoCanceler?.release() } catch (_: Exception) {}
        try { noiseSuppressor?.release() } catch (_: Exception) {}
        recorder = null
        echoCanceler = null
        noiseSuppressor = null
    }
}
