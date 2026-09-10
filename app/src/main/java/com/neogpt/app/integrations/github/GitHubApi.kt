package com.neogpt.app.integrations.github

interface GitHubApi {
    suspend fun listRepositories(): List<GitHubRepository>
    suspend fun getRepository(owner: String, repo: String): GitHubRepository
    suspend fun createBranch(owner: String, repo: String, branch: String, fromBranch: String)
    suspend fun createPullRequest(owner: String, repo: String, request: CreatePRRequest): PullRequest
    suspend fun commitFile(owner: String, repo: String, path: String, content: String, message: String)
}
