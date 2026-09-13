package com.neogpt.app.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.neogpt.app.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing
import com.neogpt.app.voice.VoiceInputManager
import com.neogpt.app.voice.VoiceState

@Composable
fun HomeScreen(onOpenDrawer: () -> Unit, onOpenChat: (String, String, String, String, String, Long) -> Unit, onOpenLive: () -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = remember { HomeViewModel(context) }
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCatalog() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var composerText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var selectedMime by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf(0L) }
    var voiceManager by remember { mutableStateOf<VoiceInputManager?>(null) }
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var voiceTranscript by remember { mutableStateOf("") }
    var voiceRmsLevel by remember { mutableStateOf(0f) }

    val recordPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) voiceManager?.startListening() }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            selectedUri = uri
            selectedMime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            selectedName = queryName(context, uri)
            selectedSize = querySize(context, uri)
        }
    }
    DisposableEffect(Unit) {
        val manager = VoiceInputManager(context); voiceManager = manager
        val job = kotlinx.coroutines.MainScope().launch {
            kotlinx.coroutines.flow.combine(manager.state, manager.transcript, manager.rmsLevel) { s, t, rms -> Triple(s, t, rms) }.collect { (s, t, rms) ->
                voiceState = s; voiceTranscript = t; voiceRmsLevel = rms
                if (s == VoiceState.IDLE && t.isNotBlank()) composerText = t
            }
        }
        onDispose { job.cancel(); manager.destroy(); voiceManager = null }
    }

    val listening = voiceState == VoiceState.LISTENING || voiceState == VoiceState.PROCESSING
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                onClick = onOpenDrawer,
            ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Menu, "Open menu", Modifier.size(24.dp)) } }
            Surface(
                shape = NeoShapes.pill,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                onClick = { showModelPicker = true },
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Text(state.selectedModel.name, style = MaterialTheme.typography.labelLarge); Spacer(Modifier.width(4.dp)); Icon(Icons.Rounded.KeyboardArrowDown, null, Modifier.size(18.dp)) }
            }
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                onClick = { onOpenChat(state.selectedModel.id, "", "", "", "", 0L) },
            ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Edit, "New chat", Modifier.size(22.dp)) } }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().navigationBarsPadding().padding(horizontal = NeoSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.neo_app_icon),
                contentDescription = "Neo GPT",
                modifier = Modifier.size(92.dp),
            )
            Spacer(Modifier.height(18.dp))
            Text("Neo GPT", style = MaterialTheme.typography.displaySmall, fontFamily = NeoFontFamily)
            Spacer(Modifier.height(8.dp))
            Text(state.greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.weight(1.25f))
            NeoComposer(
                text = composerText, onTextChange = { composerText = it },
                onSend = {
                    if (composerText.isNotBlank() || selectedUri != null) {
                        onOpenChat(state.selectedModel.id, composerText.trim(), selectedUri?.toString().orEmpty(), selectedName, selectedMime, selectedSize)
                        composerText = ""; selectedUri = null; selectedName = ""; selectedMime = ""; selectedSize = 0L
                    }
                },
                onAddClick = { filePicker.launch(arrayOf("*/*")) },
                onImageClick = {
                    if (!composerText.trimStart().startsWith("/image", ignoreCase = true)) {
                        composerText = if (composerText.isBlank()) "/image " else "/image ${composerText.trimStart()}"
                    }
                },
                onVoiceClick = { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) voiceManager?.startListening() else recordPermission.launch(Manifest.permission.RECORD_AUDIO) },
                onVoiceStop = { voiceManager?.stopListening() },
                onVoiceCancel = { voiceManager?.cancelListening(); voiceTranscript = ""; voiceRmsLevel = 0f; composerText = "" },
                isListening = listening, voiceTranscript = voiceTranscript, voiceRmsLevel = voiceRmsLevel,
                onLiveClick = onOpenLive,
                attachments = if (selectedUri != null) listOf(AttachmentChip("home", selectedName, homeAttachmentType(selectedMime))) else emptyList(),
                onRemoveAttachment = { selectedUri = null; selectedName = ""; selectedMime = ""; selectedSize = 0L },
                modifier = Modifier.padding(bottom = NeoSpacing.sm),
            )
            Text("AI can make mistakes. Check important information.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = NeoSpacing.sm))
        }
    }
    if (showModelPicker) NeoModelPickerSheet(state.availableModels, state.selectedModel.id, { viewModel.selectModel(it); showModelPicker = false }, { showModelPicker = false })
}

private fun querySize(context: Context, uri: Uri): Long {
    var size = 0L
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) }
    }
    return size
}

private fun queryName(context: Context, uri: Uri): String {
    var name = "Attachment"
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) ?: name } }
    return name
}
private fun homeAttachmentType(mime: String) = when { mime.startsWith("image/") -> AttachmentType.IMAGE; mime.startsWith("audio/") -> AttachmentType.AUDIO; mime.startsWith("video/") -> AttachmentType.VIDEO; else -> AttachmentType.FILE }
