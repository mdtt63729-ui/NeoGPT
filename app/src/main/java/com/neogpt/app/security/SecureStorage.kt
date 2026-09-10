package com.neogpt.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "neo_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveApiKey(key: String) {
        prefs.edit().putString("gemini_api_key", key).apply()
    }

    fun getApiKey(): String? = prefs.getString("gemini_api_key", null)

    fun hasApiKey(): Boolean = prefs.contains("gemini_api_key")

    fun clearApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
    }
}
