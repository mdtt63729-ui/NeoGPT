package com.neogpt.app.security

import android.content.Context
import java.security.MessageDigest

/** Offline admin gate. Clear-text credentials are not stored in the APK string table. */
class AdminAuth(context: Context) {
    private val storage = SecureStorage(context.applicationContext)
    fun isLoggedIn(): Boolean = storage.isAdminLoggedIn()
    fun login(email: String, password: String): Boolean {
        val success = MessageDigest.isEqual(sha256(email.trim().lowercase()), ADMIN_EMAIL_DIGEST) &&
            MessageDigest.isEqual(sha256(password), ADMIN_PASSWORD_DIGEST)
        if (success) storage.setAdminLoggedIn(true)
        return success
    }
    fun logout() = storage.setAdminLoggedIn(false)
    private fun sha256(value: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    private companion object {
        const val ADMIN_EMAIL_DIGEST_HEX = "93f3a7b1f769a8c69fbdbe98055e1e48ba1f9507f2fbfd7db6577197151f53f3"
        const val ADMIN_PASSWORD_DIGEST_HEX = "93f3a7b1f769a8c69fbdbe98055e1e48ba1f9507f2fbfd7db6577197151f53f3"
        val ADMIN_EMAIL_DIGEST = ADMIN_EMAIL_DIGEST_HEX.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val ADMIN_PASSWORD_DIGEST = ADMIN_PASSWORD_DIGEST_HEX.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
