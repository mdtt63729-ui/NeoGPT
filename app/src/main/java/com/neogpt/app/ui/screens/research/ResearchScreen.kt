package com.neogpt.app.ui.screens.research

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.neogpt.app.data.remote.gemini.GeminiDataSource
import com.neogpt.app.data.remote.gemini.GeminiRequestMapper
import com.neogpt.app.domain.model.Message
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.*
import com.neogpt.app.ui.theme.NeoShapes
import com.neogpt.app.ui.theme.NeoSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

@Composable
fun ResearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    NeoPage("Research", onBack) {
        NeoSectionTitle("Deep research", "Use Gemini's Google Search grounding to build a sourced answer.")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; error = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Research question") },
            minLines = 3,
        )
        Spacer(Modifier.height(NeoSpacing.md))
        Button(
            onClick = {
                val question = query.trim()
                if (question.isBlank()) return@Button
                running = true
                report = ""
                error = null
                scope.launch(Dispatchers.IO) {
                    try {
                        val key = SecureStorage(context).getApiKey().orEmpty()
                        if (key.isBlank()) error("Add a Gemini API key in Settings first.")
                        val source = GeminiDataSource(OkHttpClient(), { SecureStorage(context).getApiKey().orEmpty() })
                        val request = GeminiRequestMapper.buildRequest(
                            messages = listOf(Message(id = "research", role = Message.Role.USER, content = question)),
                            model = "gemini-3.8-flash",
                            enableSearch = true,
                        )
                        var answer = ""
                        source.streamGenerateContent("gemini-3.8-flash", request).collect { chunk ->
                            answer += chunk
                            withContext(kotlinx.coroutines.Dispatchers.Main) { report = answer }
                        }
                    } catch (t: Throwable) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) { error = t.message ?: "Research failed." }
                    } finally {
                        withContext(kotlinx.coroutines.Dispatchers.Main) { running = false }
                    }
                }
            },
            enabled = query.isNotBlank() && !running,
            modifier = Modifier.fillMaxWidth(),
            shape = NeoShapes.pill,
        ) {
            Icon(if (running) Icons.Rounded.Sync else Icons.Rounded.TravelExplore, null)
            Spacer(Modifier.width(8.dp))
            Text(if (running) "Researching…" else "Start research")
        }
        Spacer(Modifier.height(NeoSpacing.lg))
        if (error != null) {
            ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(error ?: "", Modifier.padding(NeoSpacing.lg), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        } else if (report.isBlank()) {
            NeoEmptyState(icon = Icons.Rounded.TravelExplore, title = "Ready to research", description = "Ask a focused question and Neo GPT will use live search grounding.")
        } else {
            ElevatedCard(Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(NeoSpacing.lg)) {
                    Text("Research report", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(NeoSpacing.md))
                    NeoMarkdown(report)
                }
            }
        }
    }
}
