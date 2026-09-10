package com.neogpt.app.integrations.github

class CommitService(private val api: GitHubApi) {
    suspend fun commitFile(owner: String, repo: String, path: String, content: String, message: String) =
        api.commitFile(owner, repo, path, content, message)
}
