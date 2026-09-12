package com.neogpt.app.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Small, dependency-light encrypted storage backed directly by Android Keystore.
 * This avoids startup/runtime fragility from third-party encrypted-preferences wrappers.
 */
class SecureStorage(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveApiKey(key: String) = saveProviderKey("gemini", key)

    fun getApiKey(): String? = getProviderKey("gemini")

    fun hasApiKey(): Boolean = !getApiKey().isNullOrBlank()

    fun clearApiKey() = clearProviderKey("gemini")

    fun saveProviderKey(providerId: String, key: String) {
        val cleanKey = key.trim()
        if (cleanKey.isEmpty()) {
            clearProviderKey(providerId)
            return
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(cleanKey.toByteArray(StandardCharsets.UTF_8))
        val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + SEPARATOR +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        prefs.edit().putString(providerKey(providerId), payload).apply()
    }

    fun clearProviderKey(providerId: String) {
        prefs.edit().remove(providerKey(providerId)).apply()
    }

    fun getProviderKey(providerId: String): String? {
        val storedKey = providerKey(providerId)
        val payload = prefs.getString(storedKey, null)
            ?: if (providerId == "gemini") prefs.getString(LEGACY_GEMINI_KEY, null) else null
            ?: return null
        return try {
            val parts = payload.split(SEPARATOR, limit = 2)
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(GCM_TAG_BITS, iv),
            )
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            // A reset/invalidated Keystore key must never crash app startup.
            prefs.edit().remove(providerKey(providerId)).apply()
            null
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "neo_gpt_api_key"
        const val PREFS_NAME = "neo_secure_prefs"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val SEPARATOR = ":"
        const val LEGACY_GEMINI_KEY = "gemini_api_key"

        fun providerKey(providerId: String): String = "api_key_${providerId.lowercase()}"
    }
}
