package com.neogpt.app.integrations.github

class PullRequestService(private val api: GitHubApi) {
    suspend fun createPR(owner: String, repo: String, request: CreatePRRequest) =
        api.createPullRequest(owner, repo, request)
}
