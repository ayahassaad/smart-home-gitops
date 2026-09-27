package com.ayah.smarthomemonitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for PUT /repos/{owner}/{repo}/contents/{path} — commits a
 * new version of a file. [content] must be Base64-encoded (that's how
 * GitHub's Contents API requires it), and [sha] must be the blob sha of
 * the version being replaced, or GitHub rejects the write with 409/422.
 */
data class UpdateFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: String,
    @SerializedName("sha") val sha: String,
    @SerializedName("branch") val branch: String
)
