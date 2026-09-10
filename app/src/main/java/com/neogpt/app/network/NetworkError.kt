package com.neogpt.app.network

sealed class NetworkError(message: String) : Exception(message) {
    object NoConnection : NetworkError("Check your internet connection and try again.")
    object RateLimit : NetworkError("This model is temporarily unavailable.")
    data class ServerError(val code: Int) : NetworkError("Server error: $code")
    data class Unknown(val raw: String) : NetworkError(raw)
}
