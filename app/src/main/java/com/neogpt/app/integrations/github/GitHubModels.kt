package com.neogpt.app.integrations.github

data class GitHubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val private: Boolean,
    val defaultBranch: String,
    val url: String,
)

data class PullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val state: String,
    val url: String,
)

data class CreatePRRequest(
    val title: String,
    val body: String,
    val head: String,
    val base: String,
)

data class Commit(
    val sha: String,
    val message: String,
    val author: String,
    val timestamp: String,
)
