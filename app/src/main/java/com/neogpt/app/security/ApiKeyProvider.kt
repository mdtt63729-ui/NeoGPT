package com.neogpt.app.security

class ApiKeyProvider(private val secureStorage: SecureStorage) {
    fun getApiKey(): String {
        return secureStorage.getApiKey()
            ?: throw IllegalStateException("API key not configured. Set it in Settings.")
    }

    fun isConfigured(): Boolean = secureStorage.hasApiKey()
}
