package com.neogpt.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.neogpt.app.ui.components.*
import com.neogpt.app.settings.rememberAppSettingsState
import com.neogpt.app.ui.theme.NeoSpacing
import com.neogpt.app.voice.VoiceInputManager
import com.neogpt.app.voice.VoiceState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

private data class SelectedFile(val item: PendingAttachment, val chip: AttachmentChip)

@Composable
fun ChatScreen(
    chatId: String,
    modelId: String,
    initialPrompt: String = "",
    initialAttachmentUri: String = "",
    initialAttachmentName: String = "",
    initialAttachmentMime: String = "",
    initialAttachmentSize: Long = 0L,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenLive: () -> Unit = {},
) {
    val context = LocalContext.current
    val appSettings = rememberAppSettingsState(context)
    val viewModel = remember(chatId, modelId) { ChatViewModel(context, chatId, modelId) }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showMode by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<SelectedFile?>(null) }
    var voiceManager by remember { mutableStateOf<VoiceInputManager?>(null) }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var voiceTranscript by remember { mutableStateOf("") }
    var voiceRmsLevel by remember { mutableStateOf(0f) }
    var pendingImageDownload by remember { mutableStateOf<String?>(null)}
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var moreMessageId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val recordPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voiceManager?.startListening()
    }
    val writePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingImageDownload?.let { url ->
            if (granted) viewModel.downloadImage(url)
            pendingImageDownload = null
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            selectedFile = readSelectedFile(context, uri)
        }
    }

    DisposableEffect(Unit) {
        val manager = VoiceInputManager(context)
        voiceManager = manager
        val job = kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.flow.combine(manager.state, manager.transcript, manager.rmsLevel) { s, t, rms -> Triple(s, t, rms) }.collect { (s, t, rms) ->
                voiceState = s
                voiceTranscript = t
                voiceRmsLevel = rms
                if (s == VoiceState.IDLE && t.isNotBlank()) composerText = t
            }
        }
        onDispose { job.cancel(); manager.destroy(); voiceManager = null }
    }

    LaunchedEffect(initialPrompt, initialAttachmentUri, state.isLoaded) {
        if (state.isLoaded && state.messages.isEmpty() && (initialPrompt.isNotBlank() || initialAttachmentUri.isNotBlank())) {
            val initialAttachment = if (initialAttachmentUri.isNotBlank()) {
                val uri = Uri.parse(initialAttachmentUri)
                PendingAttachment("initial", uri, initialAttachmentName.ifBlank { "Attachment" }, initialAttachmentMime.ifBlank { "application/octet-stream" }, initialAttachmentSize)
            } else null
            viewModel.sendMessage(initialPrompt, listOfNotNull(initialAttachment))
        }
    }
    // Keep the conversation pinned to the newest response while the AI streams.
    // If the user deliberately scrolls upward, we stop forcing the viewport down.
    val isNearBottom by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= (layout.totalItemsCount - 2).coerceAtLeast(0)
        }
    }
    // Never start a new animated scroll for every streamed token. That pattern
    // competes with Compose layout and causes visible flashing/jitter. We sample
    // stream growth and use a lightweight position update while the user remains
    // at the bottom; a real animation is used only for newly inserted messages.
    LaunchedEffect(appSettings.autoScroll) {
        snapshotFlow {
            state.messages.lastOrNull()?.content?.length ?: 0
        }.sample(140).collect {
            if (appSettings.autoScroll && isNearBottom && state.messages.isNotEmpty()) {
                listState.scrollToItem(state.messages.lastIndex)
            }
        }
    }

    LaunchedEffect(state.messages.size) {
        if (appSettings.autoScroll && state.messages.isNotEmpty() && isNearBottom) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }
    LaunchedEffect(state.error) { showError = state.error != null }

    val listening = voiceState == VoiceState.LISTENING || voiceState == VoiceState.PROCESSING
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoTopBar(
                title = state.modelName,
                onMenuClick = onOpenDrawer,
                showBack = true,
                onBackClick = onBack,
                showTitleSelector = false,
                actionIcon = Icons.Rounded.MoreVert,
                onActionClick = { showMode = !showMode },
            )
        },
        bottomBar = {
            Column(Modifier.imePadding().navigationBarsPadding().padding(bottom = NeoSpacing.sm)) {
                if (editingMessageId != null) {
                    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Editing message", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                            TextButton(onClick = { editingMessageId = null; composerText = "" }) { Text("Cancel") }
                        }
                    }
                }
                NeoComposer(
                    text = composerText,
                    onTextChange = { composerText = it },
                    onSend = {
                        if (composerText.isNotBlank() || selectedFile != null) {
                            val editId = editingMessageId
                            if (editId != null) viewModel.editAndResend(editId, composerText)
                            else viewModel.sendMessage(composerText, listOfNotNull(selectedFile?.item))
                            composerText = ""; selectedFile = null; editingMessageId = null
                        }
                    },
                    onAddClick = { filePicker.launch(arrayOf("*/*")) },
                    onImageClick = {
                        if (!composerText.trimStart().startsWith("/image", ignoreCase = true)) {
                            composerText = if (composerText.isBlank()) "/image " else "/image ${composerText.trimStart()}"
                        }
                    },
                    onVoiceClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) voiceManager?.startListening()
                        else recordPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onVoiceStop = { voiceManager?.stopListening() },
                    onVoiceCancel = { voiceManager?.cancelListening(); voiceTranscript = ""; voiceRmsLevel = 0f; composerText = "" },
                    isListening = listening,
                    voiceTranscript = voiceTranscript,
                    voiceRmsLevel = voiceRmsLevel,
                    onLiveClick = onOpenLive,
                    isGenerating = state.isGenerating,
                    onStop = viewModel::stopGeneration,
                    attachments = listOfNotNull(selectedFile?.chip),
                    onRemoveAttachment = { selectedFile = null },
                    activeMode = state.activeMode,
                    enterToSend = appSettings.enterToSend,
                )
            }
        },
    ) { padding ->
        if (state.messages.isEmpty()) {
            ChatWelcome(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            Box(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(top = NeoSpacing.md, bottom = 88.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        NeoMessage(
                            message = message,
                            onCopy = { viewModel.copyMessage(message.id) },
                            onRegenerate = { viewModel.regenerateMessage(message.id) },
                            onShare = { viewModel.shareMessage(message.id) },
                            onEdit = {
                                val text = viewModel.getMessageText(message.id)
                                if (message.role == MessageRole.USER) { editingMessageId = message.id; composerText = text }
                                else if (text.isNotBlank()) composerText = text
                            },
                            onLike = { viewModel.likeMessage(message.id) },
                            onDislike = { viewModel.dislikeMessage(message.id) },
                            onMore = { moreMessageId = message.id },
                            responseTextScale = appSettings.responseTextScale,
                            onOpenAttachment = { attachment ->
                                attachment.localUri?.let { uri ->
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        })
                                    }
                                }
                            },
                            onDownloadImage = {
                                message.imageUrl?.let { url ->
                                    if (android.os.Build.VERSION.SDK_INT <= 28 && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                        pendingImageDownload = url
                                        writePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        viewModel.downloadImage(url)
                                    }
                                }
                            },
                        )
                    }
                }

                // iOS-style floating "jump to latest" control. It only appears after
                // the user has moved away from the newest message.
                AnimatedVisibility(
                    visible = !isNearBottom && state.messages.isNotEmpty(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
                    enter = fadeIn(tween(180)) + androidx.compose.animation.scaleIn(initialScale = .82f, animationSpec = tween(180)),
                    exit = fadeOut(tween(140)) + androidx.compose.animation.scaleOut(targetScale = .82f, animationSpec = tween(140)),
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                        onClick = { if (state.messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(state.messages.lastIndex) } },
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.KeyboardArrowDown, "Scroll to latest", Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
    if (moreMessageId != null) {
        AlertDialog(
            onDismissRequest = { moreMessageId = null },
            title = { Text("Message actions") },
            text = { Text("Choose an action for this message.") },
            confirmButton = { TextButton(onClick = { moreMessageId?.let(viewModel::copyMessage); moreMessageId = null }) { Text("Copy") } },
            dismissButton = { TextButton(onClick = { moreMessageId?.let(viewModel::deleteMessage); moreMessageId = null }) { Text("Delete") } },
        )
    }
    if (showError && state.error != null) {
        AlertDialog(onDismissRequest = { showError = false }, icon = { Icon(Icons.Rounded.ErrorOutline, null) }, title = { Text("Neo GPT couldn't respond") }, text = { Text(state.error ?: "") }, confirmButton = { TextButton(onClick = { showError = false }) { Text("OK") } })
    }
}

@Composable
private fun ChatWelcome(modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = NeoSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("What can I help you with?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Ask anything, share a file, or start a voice conversation.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1.2f))
    }
}

private fun readSelectedFile(context: android.content.Context, uri: Uri): SelectedFile {
    var name = "Attachment"
    var size = 0L
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
        if (c.moveToFirst()) { c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) ?: name }; c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) } }
    }
    val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val id = uri.toString().hashCode().toString()
    return SelectedFile(PendingAttachment(id, uri, name, mime, size), AttachmentChip(id, name, attachmentType(mime)))
}

private fun attachmentType(mime: String): AttachmentType = when {
    mime.startsWith("image/") -> AttachmentType.IMAGE
    mime.startsWith("audio/") -> AttachmentType.AUDIO
    mime.startsWith("video/") -> AttachmentType.VIDEO
    else -> AttachmentType.FILE
}
