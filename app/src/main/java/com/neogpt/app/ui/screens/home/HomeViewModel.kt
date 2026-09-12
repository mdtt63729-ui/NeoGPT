package com.neogpt.app.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import com.neogpt.app.ai.AiProvider
import com.neogpt.app.ai.encodeModel
import com.neogpt.app.security.SecureStorage
import com.neogpt.app.ui.components.NeoModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private data class CatalogEntry(
    val provider: AiProvider,
    val id: String,
    val name: String,
    val description: String,
    val speed: String,
    val context: String = "—",
    val capabilities: List<String> = listOf("text"),
    val available: Boolean = true,
)

data class HomeUiState(
    val greeting: String = "What can I help you with?",
    val selectedModel: NeoModelInfo = HomeViewModel.catalog.first(),
    val availableModels: List<NeoModelInfo> = HomeViewModel.catalog,
    val modelsLoading: Boolean = false,
) 

class HomeViewModel(context: Context) : ViewModel() {
    private val storage = SecureStorage(context.applicationContext)
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refreshCatalog()
    }

    fun refreshCatalog() {
        val configured = AiProvider.entries.filter { !storage.getProviderKey(it.id).isNullOrBlank() }.toSet()
        val models = catalog.filter { it.provider in configured || configured.isEmpty() }
        val safeModels = if (models.isEmpty()) catalog else models
        _state.update { current ->
            val selected = safeModels.firstOrNull { it.id == current.selectedModel.id } ?: safeModels.first()
            current.copy(availableModels = safeModels, selectedModel = selected, modelsLoading = false)
        }
    }

    fun selectModel(model: NeoModelInfo) {
        if (model.isAvailable) _state.update { it.copy(selectedModel = model) }
    }

    companion object {
        private fun c(provider: AiProvider, id: String, name: String, description: String, speed: String, context: String = "—", capabilities: List<String> = listOf("text"), available: Boolean = true) =
            NeoModelInfo(encodeModel(provider, id), name, description, speed, context, capabilities, provider.displayName, available)

        val catalog: List<NeoModelInfo> = buildList {
            add(c(AiProvider.GEMINI, "gemini-3.8-flash", "Gemini 3.8 Flash", "Latest stable Flash for long-horizon reasoning, coding and agents", "Fast+", "1M", listOf("text", "vision", "audio", "pdf", "thinking")))
            add(c(AiProvider.GEMINI, "gemini-3.7-flash", "Gemini 3.7 Flash", "Stable multimodal reasoning and agentic execution", "Fast", "1M", listOf("text", "vision", "audio", "pdf", "thinking")))
            add(c(AiProvider.GEMINI, "gemini-3.7-flash-lite", "Gemini 3.7 Flash-Lite", "Requested model name; Google currently publishes Gemini 3.5 Flash-Lite instead", "Unavailable", "—", listOf("text"), available = false))
            add(c(AiProvider.GEMINI, "gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "Fastest and most cost-efficient Gemini 3 Flash tier", "Fastest", "1M", listOf("text", "vision", "thinking")))
            add(c(AiProvider.GEMINI, "gemini-3.6-flash", "Gemini 3.6 Flash", "Balanced speed and multimodal capability", "Fast", "1M", listOf("text", "vision", "audio", "pdf", "thinking")))
            add(c(AiProvider.GEMINI, "gemini-3.5-flash", "Gemini 3.5 Flash", "High-capability stable Flash for coding and agents", "Fast", "1M", listOf("text", "vision", "audio", "pdf", "thinking")))

            val openRouter = listOf(
                "inclusionai/ling-3.0-flash-fin:free" to "Ling 3.0 Flash Fin",
                "qwen/qwen3.8-flash" to "Qwen 3.8 Flash",
                "z-ai/glm-5.3-flash" to "GLM 5.3 Flash",
                "tencent/hy-mt2-30b-a3b" to "HY-MT2 30B A3B",
                "tencent/hy-mt2-1.8b" to "HY-MT2 1.8B",
                "tencent/hy-mt2-7b" to "HY-MT2 7B",
                "dots-studio/dots-3-note-preview:free" to "Dots 3 Note Preview",
                "nvidia/nemotron-3.5-lightning" to "Nemotron 3.5 Lightning",
                "meta/muse-glimmer-30b" to "Muse Glimmer 30B",
                "deepseek/deepseek-v4-flash-latest" to "DeepSeek V4 Flash Latest",
                "deepseek/deepseek-v4-flash-0731" to "DeepSeek V4 Flash 0731",
                "qwen/qwen3.7-flash" to "Qwen 3.7 Flash",
                "inclusionai/ling-3.0-flash" to "Ling 3.0 Flash",
                "poolside/laguna-s-2.1" to "Laguna S 2.1",
                "poolside/laguna-xs-2.1" to "Laguna XS 2.1",
                "nex-agi/nex-n2-mini" to "Nex N2 Mini",
                "cohere/north-mini-code:free" to "North Mini Code",
                "nvidia/nemotron-3.5-content-safety:free" to "Nemotron 3.5 Content Safety",
                "minimax/minimax-m3:free" to "MiniMax M3",
                "stepfun/step-3.7-flash" to "Step 3.7 Flash",
                "perceptron/perceptron-mk1" to "Perceptron MK1",
                "ibm-granite/granite-4.1-8b" to "Granite 4.1 8B",
                "xiaomi/mimo-v2.5" to "MiMo V2.5",
                "nvidia/nemotron-3-super-120b-a12b" to "Nemotron 3 Super 120B A12B",
                "qwen/qwen3.5-9b" to "Qwen 3.5 9B",
                "inception/mercury-2" to "Mercury 2",
                "bytedance-seed/seed-2.0-mini" to "Seed 2.0 Mini",
                "meta-llama/llama-3.1-70b-instruct" to "Llama 3.1 70B Instruct",
                "meta-llama/llama-3.1-8b-instruct" to "Llama 3.1 8B Instruct",
                "openai/gpt-4o-mini" to "GPT-4o Mini",
                "openai/gpt-4o-mini-2024-07-18" to "GPT-4o Mini 2024-07-18",
            )
            openRouter.forEach { (id, name) -> add(c(AiProvider.OPENROUTER, id, name, "OpenRouter unified model", if (id.endsWith(":free")) "Free" else "OpenRouter", capabilities = listOf("text", "vision"))) }

            val nvidia = listOf(
                "nvidia/nemotron-3.5-lightning-30b-a3b" to "Nemotron 3.5 Lightning 30B A3B",
                "meta/muse-glimmer-30b" to "Muse Glimmer 30B",
                "nvidia/riva-translate-4b-instruct-v2" to "Riva Translate 4B Instruct V2",
                "nvidia/ising-calibration-1.5-31b" to "Ising Calibration 1.5 31B",
                "nvidia/nemotron-3-embed-1b" to "Nemotron 3 Embed 1B",
                "poolside/laguna-xs-2.1" to "Laguna XS 2.1",
                "minimaxai/minimax-m3" to "MiniMax M3",
                "google/diffusiongemma-26b-a4b-it" to "DiffusionGemma 26B A4B IT",
                "nvidia/nemotron-3-ultra-550b-a55b" to "Nemotron 3 Ultra 550B A55B",
                "nvidia/nemotron-3.5-content-safety" to "Nemotron 3.5 Content Safety",
                "nvidia/cosmos3-nano" to "Cosmos 3 Nano",
                "nvidia/cosmos3-nano-reasoner" to "Cosmos 3 Nano Reasoner",
                "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning" to "Nemotron 3 Nano Omni 30B A3B Reasoning",
                "nvidia/synthetic-video-detector" to "Synthetic Video Detector",
                "nvidia/active-speaker-detection" to "Active Speaker Detection",
                "nvidia/ising-calibration-1-35b-a3b" to "Ising Calibration 1 35B A3B",
                "google/gemma-4-31b-it" to "Gemma 4 31B IT",
                "nvidia/nemotron-voicechat" to "Nemotron VoiceChat",
                "nvidia/nemotron-3-super-120b-a12b" to "Nemotron 3 Super 120B A12B",
                "nvidia/cosmos-transfer2.5-2b" to "Cosmos Transfer 2.5 2B",
                "nvidia/riva-translate-4b-instruct-v1_1" to "Riva Translate 4B Instruct V1.1",
                "nvidia/streampetr" to "StreamPETR",
                "nvidia/llama-3.1-nemotron-safety-guard-8b-v3" to "Llama 3.1 Nemotron Safety Guard 8B V3",
                "openai/gpt-oss-20b" to "GPT-OSS 20B",
                "openai/gpt-oss-120b" to "GPT-OSS 120B",
                "meta/llama-guard-4-12b" to "Llama Guard 4 12B",
                "nvidia/cosmos-transfer1-7b" to "Cosmos Transfer1 7B",
                "nvidia/background-noise-removal" to "Background Noise Removal",
                "mistralai/mistral-nemotron" to "Mistral Nemotron",
                "nvidia/magpie-tts-zeroshot" to "Magpie TTS ZeroShot",
                "nvidia/sparsedrive" to "SparseDrive",
                "nvidia/bevformer" to "BEVFormer",
                "nvidia/studio-voice" to "Studio Voice",
                "meta/llama-3.2-11b-vision-instruct" to "Llama 3.2 11B Vision Instruct",
                "meta/llama-3.2-90b-vision-instruct" to "Llama 3.2 90B Vision Instruct",
                "google/paligemma" to "PaliGemma",
                "nvidia/nemotron-parse-2.0" to "Nemotron Parse 2.0",
                "deepseek-ai/deepseek-v4-pro-0813" to "DeepSeek V4 Pro 0813",
                "deepseek-ai/deepseek-v4-flash-0731" to "DeepSeek V4 Flash 0731",
                "moonshotai/kimi-k3" to "Kimi K3",
            )
            val nonChat = setOf("riva-translate", "embed", "ising-calibration", "diffusiongemma", "cosmos3", "synthetic-video", "active-speaker", "cosmos-transfer", "streampetr", "background-noise", "magpie-tts", "sparsedrive", "bevformer", "studio-voice", "paligemma", "voicechat", "nemotron-parse")
            nvidia.forEach { (id, name) ->
                val unavailable = nonChat.any { token -> id.contains(token) }
                add(c(AiProvider.NVIDIA, id, name, if (unavailable) "NVIDIA specialized catalogue model — not a standard chat model" else "NVIDIA NIM OpenAI-compatible model", if (unavailable) "Specialized" else "NIM", capabilities = if (unavailable) listOf("specialized") else listOf("text", "vision"), available = !unavailable))
            }
        }
    }
}
