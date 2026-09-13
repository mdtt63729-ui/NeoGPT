package com.neogpt.app.live

import android.util.Base64
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct Gemini Live WebSocket transport for the Conversation screen.
 *
 * The transport deliberately stays independent from normal text chat. It uses
 * the same encrypted Gemini key already stored by Neo GPT's Settings screen.
 */
class GeminiLiveRepository(
    private val apiKey: String,
    private val model: String = DEFAULT_MODEL,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private var socket: WebSocket? = null
    private var setupComplete = false
    private var userClosing = false

    private val _connection = MutableStateFlow(LiveConnectionState.IDLE)
    val connection: StateFlow<LiveConnectionState> = _connection.asStateFlow()

    private val _assistantAudio = MutableSharedFlow<ByteArray>(extraBufferCapacity = 128)
    val assistantAudio: SharedFlow<ByteArray> = _assistantAudio.asSharedFlow()

    private val _events = MutableSharedFlow<GeminiLiveEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<GeminiLiveEvent> = _events.asSharedFlow()

    fun connect() {
        if (_connection.value == LiveConnectionState.CONNECTING || _connection.value == LiveConnectionState.CONNECTED) return
        if (apiKey.isBlank()) {
            emitError("Gemini API key is not configured. Open Settings → API & Secrets and add your Gemini key.")
            return
        }

        disconnectSilently()
        userClosing = false
        setupComplete = false
        _connection.value = LiveConnectionState.CONNECTING

        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=${java.net.URLEncoder.encode(apiKey, "UTF-8")}" 
        val request = Request.Builder().url(url).build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _events.tryEmit(GeminiLiveEvent.Status("Connected to Gemini Live"))
                sendSetup(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                handleMessage(bytes.utf8())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val message = when (response?.code) {
                    401, 403 -> "Gemini rejected the API key. Check the Gemini key in Settings."
                    404 -> "Gemini Live is unavailable for this key or model."
                    429 -> "Gemini rate limit reached. Please wait a moment and try again."
                    else -> t.message?.takeIf { it.isNotBlank() }?.let { "Gemini Live connection failed: $it" }
                        ?: "Gemini Live connection failed. Check your internet connection and Live API access."
                }
                emitError(message)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (_connection.value != LiveConnectionState.ERROR) {
                    _connection.value = LiveConnectionState.IDLE
                    if (!userClosing && reason.isNotBlank()) {
                        _events.tryEmit(GeminiLiveEvent.Status(reason))
                    }
                }
                setupComplete = false
                socket = null
            }
        })
    }

    private fun sendSetup(webSocket: WebSocket) {
        val setup = JSONObject()
            .put("model", "models/$model")
            .put("responseModalities", JSONArray().put("AUDIO"))
            .put(
                "thinkingConfig",
                JSONObject()
                    .put("thinkingLevel", "minimal")
                    .put("includeThoughts", false),
            )
            .put("inputAudioTranscription", JSONObject())
            .put("outputAudioTranscription", JSONObject())
            .put(
                "speechConfig",
                JSONObject().put(
                    "voiceConfig",
                    JSONObject().put(
                        "prebuiltVoiceConfig",
                        JSONObject().put("voiceName", "Kore"),
                    ),
                ),
            )
            .put(
                "realtimeInputConfig",
                JSONObject().put(
                    "automaticActivityDetection",
                    JSONObject()
                        .put("disabled", false)
                        .put("prefixPaddingMs", 180)
                        .put("silenceDurationMs", 420),
                ).put("activityHandling", "START_OF_ACTIVITY_INTERRUPTS"),
            )
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            """
                            You are Neo GPT's real-time voice companion.
                            Speak naturally, warmly and concisely. Respond as soon as the user's intent is clear.
                            Never reveal hidden chain-of-thought or describe private reasoning. Do not say you are thinking or processing.
                            The user may interrupt you at any time. If interrupted, stop immediately and respond to the new turn.
                            Prefer short spoken sentences over long monologues. Use the user's language naturally, including Bengali, Hindi and English when appropriate.
                            """.trimIndent(),
                        ),
                    ),
                ),
            )

        val message = JSONObject().put("setup", setup).toString()
        if (!webSocket.send(message)) emitError("Could not send the Gemini Live setup message.")
    }

    fun sendAudio(pcm16: ByteArray) {
        if (!setupComplete || _connection.value != LiveConnectionState.CONNECTED) return
        if (pcm16.isEmpty()) return
        val encoded = Base64.encodeToString(pcm16, Base64.NO_WRAP)
        val message = JSONObject().put(
            "realtimeInput",
            JSONObject().put(
                "audio",
                JSONObject()
                    .put("data", encoded)
                    .put("mimeType", "audio/pcm;rate=16000"),
            ),
        )
        socket?.send(message.toString())
    }

    fun sendText(text: String) {
        if (!setupComplete || _connection.value != LiveConnectionState.CONNECTED || text.isBlank()) return
        // Gemini 3.1 Live uses realtimeInput for incremental text during an active session.
        val message = JSONObject().put(
            "realtimeInput",
            JSONObject().put("text", text.trim()),
        )
        socket?.send(message.toString())
    }

    fun disconnect() {
        userClosing = true
        socket?.close(1000, "User ended conversation")
        socket = null
        setupComplete = false
        _connection.value = LiveConnectionState.IDLE
    }

    private fun disconnectSilently() {
        try { socket?.close(1000, "Replacing session") } catch (_: Exception) {}
        socket = null
    }

    private fun handleMessage(text: String) {
        try {
            val root = JSONObject(text)

            root.optJSONObject("error")?.let { error ->
                val code = error.optInt("code", -1)
                val message = error.optString("message", "Gemini Live returned an unknown error.")
                val friendly = when (code) {
                    401, 403 -> "Gemini rejected the API key. Check the Gemini key in Settings."
                    404 -> "Gemini Live model is unavailable for this API key."
                    429 -> "Gemini rate limit reached. Please wait and try again."
                    else -> "Gemini Live error: $message"
                }
                emitError(friendly)
                return
            }

            if (root.has("setupComplete")) {
                setupComplete = true
                _connection.value = LiveConnectionState.CONNECTED
                _events.tryEmit(GeminiLiveEvent.Ready)
            }

            val server = root.optJSONObject("serverContent") ?: return

            if (server.optBoolean("interrupted", false)) {
                _events.tryEmit(GeminiLiveEvent.Interrupted)
            }

            server.optJSONObject("inputTranscription")?.optString("text")?.takeIf { it.isNotBlank() }?.let {
                _events.tryEmit(GeminiLiveEvent.UserTranscript(it, false))
            }
            server.optJSONObject("interimInputTranscription")?.optString("text")?.takeIf { it.isNotBlank() }?.let {
                _events.tryEmit(GeminiLiveEvent.UserTranscript(it, true))
            }
            server.optJSONObject("outputTranscription")?.optString("text")?.takeIf { it.isNotBlank() }?.let {
                _events.tryEmit(GeminiLiveEvent.AssistantTranscript(it))
            }

            val modelTurn = server.optJSONObject("modelTurn")
            val parts = modelTurn?.optJSONArray("parts")
            if (parts != null) {
                for (index in 0 until parts.length()) {
                    val part = parts.optJSONObject(index) ?: continue
                    val inlineData = part.optJSONObject("inlineData") ?: continue
                    val data = inlineData.optString("data")
                    if (data.isNotBlank()) {
                        _events.tryEmit(GeminiLiveEvent.AssistantAudioStarted)
                        _assistantAudio.tryEmit(Base64.decode(data, Base64.NO_WRAP))
                    }
                }
            }

            if (server.optBoolean("turnComplete", false)) {
                _events.tryEmit(GeminiLiveEvent.TurnComplete)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to parse Gemini Live message", error)
            emitError("Received an invalid Gemini Live response.")
        }
    }

    private fun emitError(message: String) {
        _connection.value = LiveConnectionState.ERROR
        setupComplete = false
        _events.tryEmit(GeminiLiveEvent.Error(message))
    }

    companion object {
        const val DEFAULT_MODEL = "gemini-3.1-flash-live-preview"
        private const val TAG = "NeoGeminiLive"
    }
}

enum class LiveConnectionState { IDLE, CONNECTING, CONNECTED, ERROR }

sealed interface GeminiLiveEvent {
    data object Ready : GeminiLiveEvent
    data object Interrupted : GeminiLiveEvent
    data object AssistantAudioStarted : GeminiLiveEvent
    data object TurnComplete : GeminiLiveEvent
    data class UserTranscript(val text: String, val interim: Boolean) : GeminiLiveEvent
    data class AssistantTranscript(val text: String) : GeminiLiveEvent
    data class Status(val text: String) : GeminiLiveEvent
    data class Error(val message: String) : GeminiLiveEvent
}
