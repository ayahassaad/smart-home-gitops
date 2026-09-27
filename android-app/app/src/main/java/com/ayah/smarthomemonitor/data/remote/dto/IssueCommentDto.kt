package com.ayah.smarthomemonitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Minimal shape of a GitHub "Issue Comment" object, as returned by
 * GET /repos/{owner}/{repo}/issues/{pull_number}/comments.
 *
 * GitHub models every Pull Request as an Issue under the hood, so PR
 * comments are fetched through the issues/comments endpoint.
 */
data class IssueCommentDto(
    @SerializedName("id") val id: Long,
    @SerializedName("body") val body: String,
    @SerializedName("user") val user: GitHubUserDto?,
    @SerializedName("created_at") val createdAt: String?
)

data class GitHubUserDto(
    @SerializedName("login") val login: String
)
