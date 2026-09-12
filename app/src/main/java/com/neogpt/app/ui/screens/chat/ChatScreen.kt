package com.neogpt.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoSpacing
import com.neogpt.app.voice.VoiceInputManager
import com.neogpt.app.voice.VoiceState
import kotlinx.coroutines.flow.collect

private data class SelectedFile(val item: PendingAttachment, val chip: AttachmentChip)

@Composable
fun ChatScreen(
    chatId: String,
    modelId: String,
    initialPrompt: String = "",
    initialAttachmentUri: String = "",
    initialAttachmentName: String = "",
    initialAttachmentMime: String = "",
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenLive: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel = remember(modelId) { ChatViewModel(context, modelId) }
    val state by viewModel.state.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showMode by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<SelectedFile?>(null) }
    var voiceManager by remember { mutableStateOf<VoiceInputManager?>(null) }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var voiceTranscript by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val recordPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voiceManager?.startListening()
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
            kotlinx.coroutines.flow.combine(manager.state, manager.transcript) { s, t -> s to t }.collect { (s, t) ->
                voiceState = s
                voiceTranscript = t
                if (s == VoiceState.IDLE && t.isNotBlank()) composerText = t
            }
        }
        onDispose { job.cancel(); manager.destroy(); voiceManager = null }
    }

    LaunchedEffect(initialPrompt, initialAttachmentUri) {
        if (state.messages.isEmpty() && (initialPrompt.isNotBlank() || initialAttachmentUri.isNotBlank())) {
            val initialAttachment = if (initialAttachmentUri.isNotBlank()) {
                val uri = Uri.parse(initialAttachmentUri)
                PendingAttachment("initial", uri, initialAttachmentName.ifBlank { "Attachment" }, initialAttachmentMime.ifBlank { "application/octet-stream" }, 0)
            } else null
            viewModel.sendMessage(initialPrompt, listOfNotNull(initialAttachment))
        }
    }
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) { if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex) }
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
                AnimatedVisibility(showMode) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = NeoSpacing.lg, vertical = NeoSpacing.xs), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComposerMode.values().filter { it != ComposerMode.DEFAULT }.forEach { mode ->
                            FilterChip(selected = state.activeMode == mode, onClick = { }, label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) })
                        }
                    }
                }
                NeoComposer(
                    text = composerText,
                    onTextChange = { composerText = it },
                    onSend = {
                        if (composerText.isNotBlank() || selectedFile != null) {
                            viewModel.sendMessage(composerText, listOfNotNull(selectedFile?.item))
                            composerText = ""; selectedFile = null
                        }
                    },
                    onAddClick = { filePicker.launch(arrayOf("image/*", "application/pdf", "text/*", "audio/*", "video/*", "application/octet-stream")) },
                    onVoiceClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) voiceManager?.startListening()
                        else recordPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onVoiceStop = { voiceManager?.stopListening() },
                    isListening = listening,
                    voiceTranscript = voiceTranscript,
                    onLiveClick = onOpenLive,
                    isGenerating = state.isGenerating,
                    onStop = viewModel::stopGeneration,
                    attachments = listOfNotNull(selectedFile?.chip),
                    onRemoveAttachment = { selectedFile = null },
                    activeMode = state.activeMode,
                )
            }
        },
    ) { padding ->
        if (state.messages.isEmpty()) {
            ChatWelcome(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), state = listState, contentPadding = PaddingValues(vertical = NeoSpacing.md)) {
                items(state.messages, key = { it.id }) { message ->
                    NeoMessage(message, { viewModel.copyMessage(message.id) }, { viewModel.regenerateMessage(message.id) }, { viewModel.shareMessage(message.id) }, { viewModel.editMessage(message.id) }, { viewModel.likeMessage(message.id) }, { viewModel.dislikeMessage(message.id) })
                }
            }
        }
    }
    if (showError && state.error != null) {
        AlertDialog(onDismissRequest = { showError = false }, icon = { Icon(Icons.Rounded.ErrorOutline, null) }, title = { Text("Neo GPT couldn't respond") }, text = { Text(state.error ?: "") }, confirmButton = { TextButton(onClick = { showError = false }) { Text("OK") } })
    }
}

@Composable
private fun ChatWelcome(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chat-welcome")
    val scale by transition.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "chat-welcome-scale")
    Column(modifier.padding(horizontal = NeoSpacing.lg), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Surface(Modifier.size(84.dp).graphicsLayer { scaleX = scale; scaleY = scale }, shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 3.dp) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.neogpt.app.R.drawable.neo_app_icon),
                contentDescription = null,
                modifier = Modifier.padding(10.dp).fillMaxSize(),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("Neo GPT", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text("What can I help you with?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
