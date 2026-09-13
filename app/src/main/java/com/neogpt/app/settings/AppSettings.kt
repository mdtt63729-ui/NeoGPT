package com.neogpt.app.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("neo_ui_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(readState())
    val state: StateFlow<State> = _state.asStateFlow()

    fun setThemeMode(v: ThemeMode) = update { it.copy(themeMode = v) }
    fun setDynamicColor(v: Boolean) = update { it.copy(dynamicColor = v) }
    fun setAnimations(v: Boolean) = update { it.copy(animations = v) }
    fun setAutoScroll(v: Boolean) = update { it.copy(autoScroll = v) }
    fun setHaptics(v: Boolean) = update { it.copy(haptics = v) }
    fun setEnterToSend(v: Boolean) = update { it.copy(enterToSend = v) }
    fun setShowTimestamps(v: Boolean) = update { it.copy(showTimestamps = v) }
    fun setResponseTextScale(v: Float) = update { it.copy(responseTextScale = v.coerceIn(0.80f, 1.40f)) }

    private fun update(transform: (State) -> State) {
        val next = transform(_state.value)
        _state.value = next
        prefs.edit()
            .putString("theme", next.themeMode.name)
            .putBoolean("dynamic", next.dynamicColor)
            .putBoolean("animations", next.animations)
            .putBoolean("auto_scroll", next.autoScroll)
            .putBoolean("haptics", next.haptics)
            .putBoolean("enter_send", next.enterToSend)
            .putBoolean("timestamps", next.showTimestamps)
            .putFloat("response_text_scale", next.responseTextScale)
            .apply()
    }

    private fun readState(): State = State(
        themeMode = runCatching { ThemeMode.valueOf(prefs.getString("theme", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name) }.getOrDefault(ThemeMode.SYSTEM),
        dynamicColor = prefs.getBoolean("dynamic", true),
        animations = prefs.getBoolean("animations", true),
        autoScroll = prefs.getBoolean("auto_scroll", true),
        haptics = prefs.getBoolean("haptics", true),
        enterToSend = prefs.getBoolean("enter_send", false),
        showTimestamps = prefs.getBoolean("timestamps", false),
        responseTextScale = prefs.getFloat("response_text_scale", 1f).coerceIn(0.80f, 1.40f),
    )

    data class State(
        val themeMode: ThemeMode,
        val dynamicColor: Boolean,
        val animations: Boolean,
        val autoScroll: Boolean,
        val haptics: Boolean,
        val enterToSend: Boolean,
        val showTimestamps: Boolean,
        val responseTextScale: Float = 1f,
    )

    enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

    companion object {
        @Volatile private var instance: AppSettings? = null
        fun get(context: Context): AppSettings = instance ?: synchronized(this) {
            instance ?: AppSettings(context).also { instance = it }
        }
    }
}

@Composable
fun rememberAppSettingsState(context: Context): AppSettings.State =
    AppSettings.get(context).state.collectAsState().value
