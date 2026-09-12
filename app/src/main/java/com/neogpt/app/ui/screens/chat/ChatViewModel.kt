package com.neogpt.app.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neogpt.app.ai.AiModelRef
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.neo.NeoAlphaDataSource
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.data.remote.openai.OpenAiCompatibleDataSource
import com.neogpt.app.domain.model.Attachment
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
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

 data class PendingAttachment(val id: String, val uri: Uri, val name: String, val mimeType: String, val sizeBytes: Long)

data class ChatUiState(
    val messages: List<NeoMessageData> = emptyList(),
    val isGenerating: Boolean = false,
    val modelName: String = NeoAlphaDataSource.DISPLAY_NAME,
    val modelId: String = "neo:neo-4.1-alpha",
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
    private val modelRef = AiModelRef.parse(modelId)
    private val _state = MutableStateFlow(ChatUiState(modelId = modelId, modelName = modelDisplayName(modelId)))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    private var generationJob: Job? = null

    fun sendMessage(text: String, pendingAttachments: List<PendingAttachment> = emptyList()) {
        if ((text.isBlank() && pendingAttachments.isEmpty()) || _state.value.isGenerating) return
        if (modelRef.provider.name == "GEMINI" && modelRef.modelId == "none") {
            _state.update { it.copy(error = "No AI provider is configured. Open Settings and add an API key, or use Admin login to unlock Neo 4.1 Alpha.") }
            return
        }
        val prompt = text.trim()
        val user = NeoMessageData(
            id = "${System.currentTimeMillis()}-u",
            role = MessageRole.USER,
            content = prompt.ifBlank { "Attached ${pendingAttachments.size} file(s)" },
        )
        val aiId = "${System.currentTimeMillis()}-a"
        val ai = NeoMessageData(aiId, MessageRole.AI, "", isStreaming = true)
        _state.update { it.copy(messages = it.messages + user + ai, isGenerating = true, error = null) }
        generationJob?.cancel()
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                when {
                    prompt.trimStart().startsWith("/image", ignoreCase = true) -> generateNeoImage(aiId, prompt)
                    modelRef.provider == AiProvider.NEO_ALPHA -> generateNeoAlpha(aiId, prompt)
                    else -> when (modelRef.provider) {
                        AiProvider.GEMINI -> generateGemini(aiId, pendingAttachments)
                        AiProvider.OPENROUTER, AiProvider.NVIDIA -> generateOpenAiCompatible(aiId, pendingAttachments)
                        AiProvider.NEO_ALPHA -> generateNeoAlpha(aiId, prompt)
                    }
                }
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) return@launch
                _state.update { current -> current.copy(isGenerating = false, error = t.message ?: "Unable to reach ${modelRef.provider.displayName}.", messages = current.messages.filterNot { it.id == aiId }) }
            }
        }
    }

    private suspend fun generateNeoImage(aiId: String, prompt: String) {
        val description = prompt.trim().drop(6).trim()
        if (description.isBlank()) error("Add a description after /image.")
        updateImageGenerating(aiId)
        val image = NeoAlphaDataSource(client).generateImage(description)
        updateImageResult(aiId, image.imageUrl)
    }

    private suspend fun generateNeoAlpha(aiId: String, prompt: String) {
        val source = NeoAlphaDataSource(client)
        val result = source.chat(prompt)
        val responseText = result.text.trim()
        if (responseText.startsWith("/image", ignoreCase = true)) {
            val description = responseText.substring(6).trim()
            if (description.isBlank()) error("Neo 4.1 Alpha returned an empty image description.")
            updateImageGenerating(aiId)
            val image = source.generateImage(description)
            updateImageResult(aiId, image.imageUrl)
        } else {
            updateStreaming(aiId, result.text)
            finishStreaming()
        }
    }

    private suspend fun generateGemini(aiId: String, pendingAttachments: List<PendingAttachment>) {
        val key = storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()
        if (key.isBlank()) error("Gemini API key is not configured. Open Settings and add one.")
        val source = GeminiDataSource(client, apiKeyProvider = { storage.getProviderKey(AiProvider.GEMINI.id).orEmpty() })
        val messages = historyWithPendingAttachments(pendingAttachments, source)
        val request = GeminiRequestMapper.buildRequest(messages, modelRef.modelId)
        var accumulated = ""
        source.streamGenerateContent(modelRef.modelId, request).collect { chunk ->
            accumulated += chunk
            updateStreaming(aiId, accumulated)
        }
        finishStreaming()
    }

    private suspend fun generateOpenAiCompatible(aiId: String, pendingAttachments: List<PendingAttachment>) {
        val key = storage.getProviderKey(modelRef.provider.id).orEmpty()
        if (key.isBlank()) error("${modelRef.provider.displayName} API key is not configured. Open Settings and add one.")
        val source = OpenAiCompatibleDataSource(
            client = client,
            apiKeyProvider = { storage.getProviderKey(modelRef.provider.id).orEmpty() },
            baseUrl = modelRef.provider.baseUrl,
            providerName = modelRef.provider.displayName,
        )
        val history = _state.value.messages.filter { it.content.isNotBlank() }.dropLast(1)
        val providerMessages = history.dropLast(1).map { msg ->
            OpenAiCompatibleDataSource.ProviderMessage(
                role = if (msg.role == MessageRole.USER) "user" else "assistant",
                content = msg.content,
            )
        }.toMutableList()
        val last = history.lastOrNull { it.role == MessageRole.USER }
        val lastMessage = source.buildUserMessage(
            appContext.contentResolver,
            last?.content.orEmpty().takeIf { it != "Attached ${pendingAttachments.size} file(s)" }.orEmpty(),
            pendingAttachments.map { OpenAiCompatibleDataSource.ProviderAttachment(it.uri, it.name, it.mimeType) },
        )
        providerMessages += lastMessage

        var accumulated = ""
        source.streamChat(modelRef.modelId, providerMessages).collect { chunk ->
            accumulated += chunk
            updateStreaming(aiId, accumulated)
        }
        finishStreaming()
    }

    private suspend fun historyWithPendingAttachments(
        pendingAttachments: List<PendingAttachment>,
        source: GeminiDataSource,
    ): List<Message> {
        val history = _state.value.messages.filter { it.content.isNotBlank() }.dropLast(1).map {
            Message(
                id = it.id,
                role = if (it.role == MessageRole.USER) Message.Role.USER else Message.Role.AI,
                content = it.content,
                timestamp = it.timestamp,
            )
        }.toMutableList()
        if (history.isNotEmpty() && history.last().role == Message.Role.USER) {
            val last = history.removeAt(history.lastIndex)
            val attachments = pendingAttachments.map { item ->
                if (item.sizeBytes > 50L * 1024L * 1024L) error("${item.name} is larger than the 50 MB Gemini document limit.")
                val uploaded = source.uploadFile(appContext.contentResolver, item.uri, item.name, item.mimeType)
                Attachment(id = item.id, name = item.name, type = attachmentType(item.mimeType), mimeType = uploaded.mimeType ?: item.mimeType, uri = uploaded.uri, sizeBytes = item.sizeBytes)
            }
            history += last.copy(attachments = attachments)
        }
        return history
    }

    private fun updateStreaming(aiId: String, content: String) {
        _state.update { current -> current.copy(messages = current.messages.map { m -> if (m.id == aiId) m.copy(content = content, isStreaming = true) else m }) }
    }

    private fun updateImageGenerating(aiId: String) {
        _state.update { current -> current.copy(messages = current.messages.map { m -> if (m.id == aiId) m.copy(content = "", isStreaming = false, isImageGenerating = true, imageUrl = null, imageCreated = false) else m }) }
    }

    private fun updateImageResult(aiId: String, imageUrl: String) {
        _state.update { current -> current.copy(isGenerating = false, messages = current.messages.map { m -> if (m.id == aiId) m.copy(isStreaming = false, isImageGenerating = false, imageUrl = imageUrl, imageCreated = true) else m }) }
    }

    private fun finishStreaming() {
        _state.update { current -> current.copy(isGenerating = false, messages = current.messages.map { m -> if (m.isStreaming) m.copy(isStreaming = false) else m }) }
    }

    fun stopGeneration() {
        generationJob?.cancel(); generationJob = null
        _state.update { current ->
            current.copy(
                isGenerating = false,
                messages = current.messages.filterNot { it.isImageGenerating }.map { m -> if (m.isStreaming) m.copy(isStreaming = false) else m },
            )
        }
    }
    fun copyMessage(messageId: String) { val text = _state.value.messages.firstOrNull { it.id == messageId }?.content ?: return; (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Neo GPT", text)) }
    fun shareMessage(messageId: String) { val text = _state.value.messages.firstOrNull { it.id == messageId }?.content ?: return; val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; appContext.startActivity(Intent.createChooser(intent, "Share response").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    fun downloadImage(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val request = okhttp3.Request.Builder().url(imageUrl).get().build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("Image download failed (HTTP ${response.code}).")
                    val body = response.body ?: error("Generated image has no data.")
                    val resolver = appContext.contentResolver
                    val fileName = "NeoGPT_${System.currentTimeMillis()}.png"
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NeoGPT")
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                        }
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: error("Could not create a gallery file.")
                    try {
                        resolver.openOutputStream(uri)?.use { output -> body.byteStream().use { input -> input.copyTo(output) } }
                            ?: error("Could not open gallery output.")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                        }
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            android.widget.Toast.makeText(appContext, "Image saved to Pictures/NeoGPT", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (t: Throwable) {
                        resolver.delete(uri, null, null)
                        throw t
                    }
                }
            }.onFailure { error ->
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(appContext, error.message ?: "Could not download image.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun regenerateMessage(messageId: String) { val index = _state.value.messages.indexOfFirst { it.id == messageId }; if (index <= 0) return; val prompt = _state.value.messages.take(index).lastOrNull { it.role == MessageRole.USER }?.content ?: return; _state.update { it.copy(messages = it.messages.take(index)) }; sendMessage(prompt) }
    fun editMessage(messageId: String) {}
    fun likeMessage(messageId: String) {}
    fun dislikeMessage(messageId: String) {}
    override fun onCleared() { generationJob?.cancel(); client.dispatcher.executorService.shutdown(); super.onCleared() }

    companion object {
        private fun attachmentType(mime: String): Attachment.Type = when { mime.startsWith("image/") -> Attachment.Type.IMAGE; mime.startsWith("audio/") -> Attachment.Type.AUDIO; mime.startsWith("video/") -> Attachment.Type.VIDEO; else -> Attachment.Type.FILE }
        fun modelDisplayName(id: String): String {
            if (id == "neo:neo-4.1-alpha") return NeoAlphaDataSource.DISPLAY_NAME
            val ref = AiModelRef.parse(id)
            return ref.modelId.substringAfterLast('/').removeSuffix(":free").replace('-', ' ').replaceFirstChar { it.uppercase() }
        }
    }
}
