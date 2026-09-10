package com.neogpt.app.integrations.github

class RepositoryService(private val api: GitHubApi) {
    suspend fun listRepos() = api.listRepositories()
    suspend fun getRepo(owner: String, repo: String) = api.getRepository(owner, repo)
}
