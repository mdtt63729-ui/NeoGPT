package com.neogpt.app.integrations.github

class GitHubAuth {
    private var token: String? = null

    fun setToken(token: String) { this.token = token }
    fun getToken(): String? = token
    fun isAuthenticated(): Boolean = token != null
}
