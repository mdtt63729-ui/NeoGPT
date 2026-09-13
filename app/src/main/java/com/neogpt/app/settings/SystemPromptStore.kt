package com.neogpt.app.settings

import android.content.Context

/**
 * App-wide system instruction shared by every text-capable AI route.
 * It is intentionally user-editable and persisted locally on this device.
 */
class SystemPromptStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): String = prefs.getString(KEY, "").orEmpty()

    fun save(prompt: String) {
        prefs.edit().putString(KEY, prompt.trim()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val PREFS_NAME = "neo_system_prompt"
        private const val KEY = "system_prompt"
    }
}
