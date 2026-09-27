package com.ayah.smarthomemonitor.data.remote

import com.ayah.smarthomemonitor.data.remote.dto.FileContentDto
import com.ayah.smarthomemonitor.data.remote.dto.IssueCommentDto
import com.ayah.smarthomemonitor.data.remote.dto.PullRequestDto
import com.ayah.smarthomemonitor.data.remote.dto.UpdateFileRequest
import com.ayah.smarthomemonitor.data.remote.dto.UpdatePullRequestStateRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for the GitHub REST API endpoints this app needs.
 *
 * Lab 1 only used the read-only GET endpoints (listing open Pull Requests
 * and their comments). Lab 2 adds the write endpoints that let the app
 * close the GitOps loop: closing a Pull Request, and reading + overwriting
 * a file's content on `main`.
 */
interface GitHubApiService {

    @GET("repos/{owner}/{repo}/pulls")
    suspend fun getOpenPullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open"
    ): List<PullRequestDto>

    // Pull Requests are Issues under the hood in GitHub's API, so their
    // comments live at the issues/{number}/comments endpoint.
    @GET("repos/{owner}/{repo}/issues/{pull_number}/comments")
    suspend fun getIssueComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): List<IssueCommentDto>

    // --- Lab 2: write endpoints ------------------------------------------

    /**
     * Closes a Pull Request WITHOUT merging it — used by Force Reject
     * (simply denies the change), and also called by Force Merge as its
     * final step, after the config file has already been overwritten.
     */
    @PATCH("repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun closePullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int,
        @Body body: UpdatePullRequestStateRequest = UpdatePullRequestStateRequest()
    ): PullRequestDto

    /** Reads a file's current content + sha (needed before overwriting it). */
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") ref: String = "main"
    ): FileContentDto

    /** Commits a new version of a file (used by Force Merge to apply the approved config). */
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun updateFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body body: UpdateFileRequest
    ): ResponseBody
}
