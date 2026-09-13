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
import com.neogpt.app.NeoGptApplication
import com.neogpt.app.ai.AiModelRef
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.data.local.entity.ChatEntity
import com.neogpt.app.data.local.entity.MessageEntity
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.neo.NeoAlphaDataSource
import com.neogpt.app.files.AttachmentContentReader
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.data.remote.openai.OpenAiCompatibleDataSource
import com.neogpt.app.domain.model.Attachment
import com.neogpt.app.domain.model.Message
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.settings.SystemPromptStore
import com.neogpt.app.ui.components.ComposerMode
import com.neogpt.app.ui.components.MessageRole
import com.neogpt.app.ui.components.NeoMessageData
import com.neogpt.app.ui.components.AgentStep
import com.neogpt.app.ui.components.AgentStepState
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.UUID
import java.util.concurrent.TimeUnit

 data class PendingAttachment(val id: String, val uri: Uri, val name: String, val mimeType: String, val sizeBytes: Long)

data class ChatUiState(
    val messages: List<NeoMessageData> = emptyList(),
    val isGenerating: Boolean = false,
    val isLoaded: Boolean = false,
    val modelName: String = NeoAlphaDataSource.DISPLAY_NAME,
    val modelId: String = "neo:neo-4.1-alpha",
    val activeMode: ComposerMode? = null,
    val error: String? = null,
)

class ChatViewModel(context: Context, requestedChatId: String, modelId: String) : ViewModel() {
    private val appContext = context.applicationContext
    private val storage = SecureStorage(appContext)
    private val systemPromptStore = SystemPromptStore(appContext)
    private val database = (appContext as NeoGptApplication).database
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val attachmentAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        .adapter<List<Attachment>>(Types.newParameterizedType(List::class.java, Attachment::class.java))
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val modelRef = AiModelRef.parse(modelId)
    private val activeChatId = requestedChatId.takeIf { it.isNotBlank() && it != "new" } ?: UUID.randomUUID().toString()
    private val _state = MutableStateFlow(ChatUiState(modelId = modelId, modelName = modelDisplayName(modelId)))
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    private var generationJob: Job? = null

    init { viewModelScope.launch(Dispatchers.IO) { loadHistory() } }

    private suspend fun loadHistory() {
        val messages = messageDao.getMessagesForChatOnce(activeChatId).map { it.toUiMessage() }
        _state.update { it.copy(messages = messages, isLoaded = true) }
    }

    fun sendMessage(text: String, pendingAttachments: List<PendingAttachment> = emptyList()) {
        if ((text.isBlank() && pendingAttachments.isEmpty()) || _state.value.isGenerating) return
        if (modelRef.provider == AiProvider.GEMINI && modelRef.modelId == "none") {
            _state.update { it.copy(error = "No AI provider is configured. Open Settings and add an API key, or use Admin login to unlock Neo 4.1 Alpha.") }
            return
        }
        val prompt = text.trim()
        val user = NeoMessageData(
            id = "${System.currentTimeMillis()}-u",
            role = MessageRole.USER,
            content = prompt,
            attachments = pendingAttachments.map { it.toAttachment() },
        )
        val aiId = "${System.currentTimeMillis()}-a"
        val largeTask = isLargeTask(prompt, pendingAttachments)
        val steps = if (largeTask) initialThinkingSteps(pendingAttachments) else emptyList()
        val ai = NeoMessageData(
            id = aiId,
            role = MessageRole.AI,
            content = "",
            isStreaming = true,
            agentSteps = steps,
            toolLabels = if (largeTask) pendingAttachments.map { "Read File • ${it.name}" } else emptyList(),
        )
        _state.update { it.copy(messages = it.messages + user + ai, isGenerating = true, error = null) }
        generationJob?.cancel()
        generationJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                ensureChat(prompt, pendingAttachments)
                persistUserAndAi(user, aiId)
                if (!largeTask) delay(180)
                when {
                    prompt.trimStart().startsWith("/image", ignoreCase = true) -> generateNeoImage(aiId, prompt)
                    modelRef.provider == AiProvider.NEO_ALPHA -> generateNeoAlpha(aiId, prompt, pendingAttachments)
                    modelRef.provider == AiProvider.GEMINI -> generateGemini(aiId, pendingAttachments)
                    modelRef.provider == AiProvider.OPENROUTER || modelRef.provider == AiProvider.NVIDIA -> generateOpenAiCompatible(aiId)
                }
            } catch (t: Throwable) {
                if (t is kotlinx.coroutines.CancellationException) return@launch
                val message = t.message ?: "Unable to reach ${modelRef.provider.displayName}."
                _state.update { current ->
                    current.copy(
                        isGenerating = false,
                        error = message,
                        messages = current.messages.map { item ->
                            if (item.id == aiId) item.copy(
                                content = "",
                                isStreaming = false,
                                agentSteps = item.agentSteps.mapIndexed { index, step ->
                                    if (index == item.agentSteps.lastIndex) step.copy(message = "Could not complete this step", state = AgentStepState.ERROR) else step.copy(state = if (step.state == AgentStepState.IN_PROGRESS) AgentStepState.COMPLETED else step.state)
                                },
                            ) else item
                        },
                    )
                }
            }
        }
    }

    private suspend fun ensureChat(prompt: String, attachments: List<PendingAttachment>) {
        val now = System.currentTimeMillis()
        val existing = chatDao.getChatById(activeChatId)
        if (existing == null) {
            val titleSource = prompt.ifBlank { attachments.firstOrNull()?.name ?: "New Chat" }
            val title = titleSource.replace("/image", "", ignoreCase = true).trim().ifBlank { "New Chat" }.take(60)
            chatDao.insertChat(ChatEntity(activeChatId, title, "${modelRef.provider.id}:${modelRef.modelId}", null, now, now, false, false))
        } else chatDao.touchChat(activeChatId, now)
    }

    private suspend fun persistUserAndAi(user: NeoMessageData, aiId: String) {
        messageDao.insertMessage(user.toDomainMessage().toEntity(activeChatId))
        messageDao.insertMessage(MessageEntity(aiId, activeChatId, "AI", "", "[]", System.currentTimeMillis(), true))
    }

    private suspend fun generateNeoImage(aiId: String, prompt: String) {
        val description = prompt.trim().drop(6).trim()
        if (description.isBlank()) error("Add a description after /image.")
        updateImageGenerating(aiId)
        val image = generateImageWithProgress(aiId, description)
        updateImageResult(aiId, image.imageUrl)
    }

    private suspend fun generateNeoAlpha(aiId: String, prompt: String, pendingAttachments: List<PendingAttachment>) {
        val source = NeoAlphaDataSource(client)
        updateThinkingStage(aiId, "Reviewing the request", "Reading the attached context")
        val result = source.chat(buildNeoPromptWithFiles(prompt, pendingAttachments), systemPromptStore.get())
        val responseText = result.text.trim()
        if (responseText.startsWith("/image", ignoreCase = true)) {
            val description = responseText.substring(6).trim()
            if (description.isBlank()) error("Neo 4.1 Alpha returned an empty image description.")
            updateImageGenerating(aiId)
            val image = generateImageWithProgress(aiId, description, source)
            updateImageResult(aiId, image.imageUrl)
        } else {
            updateThinkingStage(aiId, "Preparing the response", null)
            updateStreaming(aiId, result.text)
            finishStreaming(aiId)
        }
    }

    private suspend fun buildNeoPromptWithFiles(prompt: String, pending: List<PendingAttachment>): String {
        val fromHistory = _state.value.messages.flatMap { message ->
            if (message.role == MessageRole.USER) message.attachments.mapNotNull { a ->
                a.localUri?.let { PendingAttachment(a.id, Uri.parse(it), a.name, a.mimeType, a.sizeBytes) }
            } else emptyList()
        }
        val all = (pending + fromHistory).distinctBy { it.id }
        if (all.isEmpty()) return prompt
        return buildString {
            append(prompt)
            all.forEach { file ->
                if (file.sizeBytes > 15L * 1024L * 1024L) error("${file.name} is larger than the 15 MB Neo attachment limit.")
                append("\n\n--- ATTACHMENT: ${file.name} (${formatFileSize(file.sizeBytes)}) ---\n")
                append(AttachmentContentReader.readForPrompt(appContext.contentResolver, file.uri, file.name, file.mimeType))
                append("\n--- END ATTACHMENT ---")
            }
        }
    }

    private suspend fun generateGemini(aiId: String, pendingAttachments: List<PendingAttachment>) {
        val key = storage.getProviderKey(AiProvider.GEMINI.id).orEmpty()
        if (key.isBlank()) error("Gemini API key is not configured. Open Settings and add one.")
        val source = GeminiDataSource(client, apiKeyProvider = { storage.getProviderKey(AiProvider.GEMINI.id).orEmpty() })
        updateThinkingStage(aiId, "Reviewing the request", if (pendingAttachments.isNotEmpty()) "Reading the attached context" else "Preparing the response")
        val messages = historyWithAttachments(pendingAttachments, source)
        val request = GeminiRequestMapper.buildRequest(messages, modelRef.modelId, systemPrompt = systemPromptStore.get())
        updateThinkingStage(aiId, "Preparing the response", null)
        var accumulated = ""
        source.streamGenerateContent(modelRef.modelId, request).collect { chunk -> accumulated += chunk; updateStreaming(aiId, accumulated) }
        finishStreaming(aiId)
    }

    private suspend fun generateOpenAiCompatible(aiId: String) {
        val key = storage.getProviderKey(modelRef.provider.id).orEmpty()
        if (key.isBlank()) error("${modelRef.provider.displayName} API key is not configured. Open Settings and add one.")
        val source = OpenAiCompatibleDataSource(client, { storage.getProviderKey(modelRef.provider.id).orEmpty() }, modelRef.provider.baseUrl, modelRef.provider.displayName)
        updateThinkingStage(aiId, "Reviewing the request", "Preparing the provider request")
        val history = _state.value.messages.dropLast(1).filter { it.content.isNotBlank() || it.attachments.isNotEmpty() }
        val providerMessages = mutableListOf<OpenAiCompatibleDataSource.ProviderMessage>()
        for (msg in history) {
            if (msg.role == MessageRole.USER && msg.attachments.isNotEmpty()) {
                val attachments = msg.attachments.mapNotNull { a -> a.localUri?.let { OpenAiCompatibleDataSource.ProviderAttachment(Uri.parse(it), a.name, a.mimeType) } }
                providerMessages += source.buildUserMessage(appContext.contentResolver, msg.content, attachments)
            } else if (msg.content.isNotBlank()) {
                providerMessages += OpenAiCompatibleDataSource.ProviderMessage(if (msg.role == MessageRole.USER) "user" else "assistant", msg.content)
            }
        }
        updateThinkingStage(aiId, "Preparing the response", null)
        var accumulated = ""
        source.streamChat(modelRef.modelId, providerMessages, systemPrompt = systemPromptStore.get()).collect { chunk -> accumulated += chunk; updateStreaming(aiId, accumulated) }
        finishStreaming(aiId)
    }

    private suspend fun historyWithAttachments(pending: List<PendingAttachment>, source: GeminiDataSource): List<Message> {
        val history = _state.value.messages.dropLast(1).filter { it.content.isNotBlank() || it.attachments.isNotEmpty() }.map { it.toDomainMessage() }.toMutableList()
        val lastUserIndex = history.indexOfLast { it.role == Message.Role.USER }
        if (lastUserIndex >= 0 && pending.isNotEmpty()) {
            val last = history[lastUserIndex]
            val uploaded = pending.map { uploadGeminiAttachment(source, it) }
            val byId = uploaded.associateBy { it.id }
            history[lastUserIndex] = last.copy(attachments = last.attachments.map { byId[it.id] ?: it } + uploaded.filter { fresh -> last.attachments.none { it.id == fresh.id } })
        }
        return history
    }

    private suspend fun uploadGeminiAttachment(source: GeminiDataSource, item: PendingAttachment): Attachment {
        if (item.sizeBytes > 50L * 1024L * 1024L) error("${item.name} is larger than the 50 MB Gemini document limit.")
        val uploaded = source.uploadFile(appContext.contentResolver, item.uri, item.name, item.mimeType)
        val result = item.toAttachment().copy(uri = uploaded.uri, mimeType = uploaded.mimeType ?: item.mimeType)
        replaceAttachmentOnLatestUser(item.id, result)
        persistLatestUserAttachment()
        return result
    }

    private fun replaceAttachmentOnLatestUser(id: String, attachment: Attachment) {
        _state.update { current ->
            val index = current.messages.indexOfLast { it.role == MessageRole.USER }
            if (index < 0) return@update current
            val list = current.messages.toMutableList()
            list[index] = list[index].copy(attachments = list[index].attachments.map { if (it.id == id) attachment else it })
            current.copy(messages = list)
        }
    }

    private suspend fun persistLatestUserAttachment() {
        val user = _state.value.messages.lastOrNull { it.role == MessageRole.USER } ?: return
        val existing = messageDao.getMessagesForChatOnce(activeChatId).firstOrNull { it.id == user.id } ?: return
        messageDao.updateMessage(existing.copy(attachmentsJson = attachmentAdapter.toJson(user.attachments)))
    }

    private fun isLargeTask(prompt: String, attachments: List<PendingAttachment>): Boolean {
        if (attachments.isNotEmpty()) return true
        if (prompt.length >= 420) return true
        val keywords = listOf("build", "code", "project", "repository", "repo", "analyze", "analysis", "research", "implement", "fix", "debug", "refactor", "file", "zip", "apk", "compare", "write a full", "create a full")
        return keywords.count { prompt.contains(it, ignoreCase = true) } >= 1
    }

    private fun initialThinkingSteps(attachments: List<PendingAttachment>): List<AgentStep> = buildList {
        add(AgentStep("understand", "Understanding the request", AgentStepState.IN_PROGRESS))
        if (attachments.isNotEmpty()) add(AgentStep("files", "Reading the attached context", AgentStepState.PENDING))
        add(AgentStep("prepare", "Preparing the response", AgentStepState.PENDING))
    }

    private fun updateThinkingStage(aiId: String, activeMessage: String, nextMessage: String?) {
        _state.update { current ->
            current.copy(messages = current.messages.map { item ->
                if (item.id != aiId || item.agentSteps.isEmpty()) item else {
                    val next = item.agentSteps.map { step ->
                        when {
                            step.message == activeMessage -> step.copy(state = AgentStepState.IN_PROGRESS)
                            step.state == AgentStepState.IN_PROGRESS -> step.copy(state = AgentStepState.COMPLETED)
                            else -> step
                        }
                    }.toMutableList()
                    if (nextMessage != null) {
                        val existingIndex = next.indexOfFirst { it.message == nextMessage }
                        if (existingIndex >= 0) next[existingIndex] = next[existingIndex].copy(state = AgentStepState.IN_PROGRESS)
                    }
                    item.copy(agentSteps = next)
                }
            })
        }
    }

    private fun updateStreaming(aiId: String, content: String) {
        _state.update { current -> current.copy(messages = current.messages.map { m -> if (m.id == aiId) m.copy(content = content, isStreaming = true) else m }) }
    }

    private fun updateImageGenerating(aiId: String) {
        _state.update { current -> current.copy(messages = current.messages.map { m -> if (m.id == aiId) m.copy(content = "", isStreaming = false, isImageGenerating = true, imageUrl = null, imageCreated = false, imageProgress = 4, imageStatusText = "Preparing the canvas…", agentSteps = emptyList()) else m }) }
    }

    private suspend fun generateImageWithProgress(aiId: String, description: String, source: NeoAlphaDataSource = NeoAlphaDataSource(client)): NeoAlphaDataSource.ImageResult = coroutineScope {
        val progressJob = launch {
            var progress = 8
            while (isActive && progress < 92) {
                val status = when { progress < 28 -> "Sketching it out…"; progress < 55 -> "Building the composition…"; progress < 78 -> "Rendering details…"; else -> "Polishing the image…" }
                updateImageProgress(aiId, progress, status)
                delay(260)
                progress += if (progress < 55) 3 else 2
            }
        }
        try { source.generateImage(description).also { progressJob.cancel(); updateImageProgress(aiId, 100, "Image rendered") } }
        finally { progressJob.cancel() }
    }

    private fun updateImageProgress(aiId: String, progress: Int, status: String) {
        _state.update { current -> current.copy(messages = current.messages.map { m -> if (m.id == aiId) m.copy(imageProgress = progress.coerceIn(0, 100), imageStatusText = status) else m }) }
    }

    private suspend fun updateImageResult(aiId: String, imageUrl: String) {
        _state.update { current -> current.copy(isGenerating = false, messages = current.messages.map { m -> if (m.id == aiId) m.copy(isStreaming = false, isImageGenerating = false, imageUrl = imageUrl, imageCreated = true, imageProgress = 100, imageStatusText = "Image rendered", agentSteps = emptyList()) else m }) }
        persistAiFromState(aiId)
    }

    private suspend fun finishStreaming(aiId: String) {
        _state.update { current -> current.copy(isGenerating = false, messages = current.messages.map { m -> if (m.id == aiId) m.copy(isStreaming = false, agentSteps = emptyList()) else m }) }
        persistAiFromState(aiId)
    }

    private suspend fun persistAiFromState(aiId: String) {
        val ai = _state.value.messages.firstOrNull { it.id == aiId } ?: return
        val existing = messageDao.getMessagesForChatOnce(activeChatId).firstOrNull { it.id == aiId }
        if (existing != null) messageDao.updateMessage(existing.copy(content = ai.content, attachmentsJson = attachmentAdapter.toJson(ai.attachments), isStreaming = false, timestamp = ai.timestamp))
        else messageDao.insertMessage(MessageEntity(ai.id, activeChatId, "AI", ai.content, attachmentAdapter.toJson(ai.attachments), ai.timestamp, false))
        chatDao.touchChat(activeChatId, System.currentTimeMillis())
    }

    fun stopGeneration() {
        val partial = _state.value.messages.lastOrNull { it.role == MessageRole.AI && it.isStreaming }
        generationJob?.cancel(); generationJob = null
        _state.update { current -> current.copy(isGenerating = false, messages = current.messages.map { m -> if (m.isStreaming) m.copy(isStreaming = false) else m }) }
        partial?.let { viewModelScope.launch(Dispatchers.IO) { persistAiFromState(it.id) } }
    }

    fun getMessageText(id: String): String = _state.value.messages.firstOrNull { it.id == id }?.content.orEmpty()

    fun editAndResend(messageId: String, newText: String) {
        val message = _state.value.messages.firstOrNull { it.id == messageId } ?: return
        if (message.role != MessageRole.USER || newText.isBlank()) return
        val pending = message.attachments.mapNotNull { a -> a.localUri?.let { PendingAttachment(a.id, Uri.parse(it), a.name, a.mimeType, a.sizeBytes) } }
        val index = _state.value.messages.indexOfFirst { it.id == messageId }
        val idsToDelete = _state.value.messages.drop(index).take(2).map { it.id }
        _state.update { it.copy(messages = it.messages.take(index)) }
        viewModelScope.launch(Dispatchers.IO) { idsToDelete.forEach { messageDao.deleteMessageById(it) }; messageDao.deleteMessageById(messageId) }
        sendMessage(newText, pending)
    }

    fun deleteMessage(messageId: String) {
        _state.update { it.copy(messages = it.messages.filterNot { m -> m.id == messageId }) }
        viewModelScope.launch(Dispatchers.IO) { messageDao.deleteMessageById(messageId) }
    }

    fun copyMessage(messageId: String) {
        val text = getMessageText(messageId); if (text.isBlank()) return
        (appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Neo GPT", text))
    }

    fun shareMessage(messageId: String) {
        val text = getMessageText(messageId); if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        appContext.startActivity(Intent.createChooser(intent, "Share message").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun regenerateMessage(messageId: String) {
        val index = _state.value.messages.indexOfFirst { it.id == messageId }
        if (index <= 0) return
        val prompt = _state.value.messages.take(index).lastOrNull { it.role == MessageRole.USER } ?: return
        editAndResend(prompt.id, prompt.content)
    }

    fun likeMessage(@Suppress("UNUSED_PARAMETER") id: String) {}
    fun dislikeMessage(@Suppress("UNUSED_PARAMETER") id: String) {}

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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NeoGPT"); put(MediaStore.Images.Media.IS_PENDING, 1) }
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("Could not create a gallery file.")
                    try {
                        resolver.openOutputStream(uri)?.use { output -> body.byteStream().use { input -> input.copyTo(output) } } ?: error("Could not open gallery output.")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
                        withContext(Dispatchers.Main) { android.widget.Toast.makeText(appContext, "Image saved to Pictures/NeoGPT", android.widget.Toast.LENGTH_SHORT).show() }
                    } catch (t: Throwable) { resolver.delete(uri, null, null); throw t }
                }
            }.onFailure { error -> withContext(Dispatchers.Main) { android.widget.Toast.makeText(appContext, error.message ?: "Could not download image.", android.widget.Toast.LENGTH_SHORT).show() } }
        }
    }

    override fun onCleared() { generationJob?.cancel(); client.dispatcher.executorService.shutdown(); super.onCleared() }

    private fun PendingAttachment.toAttachment() = Attachment(id, name, attachmentType(mimeType), mimeType, localUri = uri.toString(), sizeBytes = sizeBytes)
    private fun NeoMessageData.toDomainMessage() = Message(id, if (role == MessageRole.USER) Message.Role.USER else Message.Role.AI, content, attachments, timestamp, isStreaming)
    private fun Message.toEntity(chatId: String) = MessageEntity(id, chatId, if (role == Message.Role.USER) "USER" else "AI", content, attachmentAdapter.toJson(attachments), timestamp, isStreaming)
    private fun MessageEntity.toUiMessage(): NeoMessageData {
        val attachments = runCatching { attachmentAdapter.fromJson(attachmentsJson).orEmpty() }.getOrDefault(emptyList())
        return NeoMessageData(id, if (role == "USER") MessageRole.USER else MessageRole.AI, content, isStreaming, timestamp, attachments = attachments)
    }

    companion object {
        private fun attachmentType(mime: String): Attachment.Type = when { mime.startsWith("image/") -> Attachment.Type.IMAGE; mime.startsWith("audio/") -> Attachment.Type.AUDIO; mime.startsWith("video/") -> Attachment.Type.VIDEO; else -> Attachment.Type.FILE }
        fun formatFileSize(bytes: Long): String = when { bytes < 1024L -> "$bytes B"; bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0); bytes < 1024L * 1024L * 1024L -> String.format("%.1f MB", bytes / (1024.0 * 1024.0)); else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0)) }
        fun modelDisplayName(id: String): String {
            if (id == "neo:neo-4.1-alpha") return NeoAlphaDataSource.DISPLAY_NAME
            val ref = AiModelRef.parse(id)
            return ref.modelId.substringAfterLast('/').removeSuffix(":free").replace('-', ' ').replaceFirstChar { it.uppercase() }
        }
    }
}
