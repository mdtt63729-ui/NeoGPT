package com.neogpt.app.live

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Low-latency 24 kHz mono PCM16 playback for Gemini Live native audio. */
class LiveAudioOutput(
    private val scope: CoroutineScope,
) {
    private val queue = LinkedBlockingQueue<ByteArray>(40)
    private var track: AudioTrack? = null
    private var playbackJob: Job? = null
    private var running = false

    fun start() {
        if (track != null) return
        val sampleRate = 24_000
        val format = AudioFormat.ENCODING_PCM_16BIT
        val channel = AudioFormat.CHANNEL_OUT_MONO
        val min = AudioTrack.getMinBufferSize(sampleRate, channel, format).coerceAtLeast(4096)

        track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(format)
                    .setChannelMask(channel)
                    .build(),
            )
            .setBufferSizeInBytes(min * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        running = true
        track?.play()
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.IO) {
            while (isActive && running) {
                try {
                    val data = queue.poll(60, TimeUnit.MILLISECONDS) ?: continue
                    track?.write(data, 0, data.size, AudioTrack.WRITE_BLOCKING)
                } catch (error: Exception) {
                    Log.e("NeoLiveAudio", "Audio playback failed", error)
                }
            }
        }
    }

    fun enqueue(pcm: ByteArray) {
        if (pcm.isEmpty()) return
        start()
        if (!queue.offer(pcm)) {
            queue.poll()
            queue.offer(pcm)
        }
    }

    fun interrupt() {
        queue.clear()
        try {
            track?.pause()
            track?.flush()
            track?.play()
        } catch (_: Exception) {}
    }

    fun release() {
        running = false
        queue.clear()
        playbackJob?.cancel()
        playbackJob = null
        try { track?.pause() } catch (_: Exception) {}
        try { track?.flush() } catch (_: Exception) {}
        try { track?.release() } catch (_: Exception) {}
        track = null
    }
}
