package com.neogpt.app.ui.screens.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.neogpt.app.R
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.theme.NeoFontFamily
import com.neogpt.app.ui.theme.NeoShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun ApiSetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val storage = remember { SecureStorage(context) }
    val scope = rememberCoroutineScope()
    var gemini by remember { mutableStateOf("") }
    var openRouter by remember { mutableStateOf("") }
    var nvidia by remember { mutableStateOf("") }
    var visibleProvider by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.Center,
        ) {
            ImageHeader()
            Text("Connect Neo GPT", style = MaterialTheme.typography.displaySmall, fontFamily = NeoFontFamily)
            Spacer(Modifier.height(8.dp))
            Text("Add one or more AI provider keys. Neo GPT keeps them encrypted on this device and lets you switch models from the home screen.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))

            ProviderKeyCard(AiProvider.GEMINI, gemini, { gemini = it }, visibleProvider == "gemini", { visibleProvider = if (visibleProvider == "gemini") null else "gemini" }, busy == "gemini") {
                verifyAndSave(context, storage, scope, AiProvider.GEMINI, gemini, { busy = "gemini" }, { busy = null; message = it })
            }
            Spacer(Modifier.height(10.dp))
            ProviderKeyCard(AiProvider.OPENROUTER, openRouter, { openRouter = it }, visibleProvider == "openrouter", { visibleProvider = if (visibleProvider == "openrouter") null else "openrouter" }, busy == "openrouter") {
                verifyAndSave(context, storage, scope, AiProvider.OPENROUTER, openRouter, { busy = "openrouter" }, { busy = null; message = it })
            }
            Spacer(Modifier.height(10.dp))
            ProviderKeyCard(AiProvider.NVIDIA, nvidia, { nvidia = it }, visibleProvider == "nvidia", { visibleProvider = if (visibleProvider == "nvidia") null else "nvidia" }, busy == "nvidia") {
                verifyAndSave(context, storage, scope, AiProvider.NVIDIA, nvidia, { busy = "nvidia" }, { busy = null; message = it })
            }

            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(message!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(54.dp), shape = NeoShapes.pill, enabled = gemini.isNotBlank() || openRouter.isNotBlank() || nvidia.isNotBlank()) {
                Text("Continue to Neo GPT")
            }
            Spacer(Modifier.height(8.dp))
            Text("You can add or change providers later in Settings.", Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ImageHeader() {
    androidx.compose.foundation.Image(painterResource(R.drawable.neo_app_icon), null, Modifier.size(68.dp).clip(CircleShape))
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun ProviderKeyCard(
    provider: AiProvider,
    key: String,
    onKeyChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
    busy: Boolean,
    onVerify: () -> Unit,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, null, Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(provider.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(if (provider == AiProvider.GEMINI) "Direct Google API" else "OpenAI-compatible API", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (key.isNotBlank()) Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = key,
                onValueChange = onKeyChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("${provider.displayName} API key") },
                placeholder = { Text("Paste your key") },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = onToggle) { Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = NeoShapes.large,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onVerify, enabled = key.isNotBlank() && !busy, shape = NeoShapes.pill) {
                if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Verify & save")
            }
        }
    }
}

private fun verifyAndSave(
    context: Context,
    storage: SecureStorage,
    scope: kotlinx.coroutines.CoroutineScope,
    provider: AiProvider,
    key: String,
    onStart: () -> Unit,
    onResult: (String) -> Unit,
) {
    onStart()
    scope.launch {
        val result = withContext(Dispatchers.IO) { verifyProvider(provider, key.trim()) }
        result.onSuccess {
            storage.saveProviderKey(provider.id, key.trim())
            onResult("${provider.displayName} connected successfully.")
        }.onFailure { onResult(it.message ?: "Could not verify ${provider.displayName}.") }
    }
}

private fun verifyProvider(provider: AiProvider, key: String): Result<Unit> = runCatching {
    val endpoint = when (provider) {
        AiProvider.GEMINI -> "https://generativelanguage.googleapis.com/v1beta/models?key=$key"
        AiProvider.OPENROUTER -> "https://openrouter.ai/api/v1/models"
        AiProvider.NVIDIA -> "https://integrate.api.nvidia.com/v1/models"
    }
    val request = Request.Builder().url(endpoint).header("Authorization", "Bearer $key").get().build()
    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) error("${provider.displayName} rejected the key (${response.code}).")
    }
}
