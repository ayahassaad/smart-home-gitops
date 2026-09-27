package com.ayah.smarthomemonitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Shape of GitHub's "Get repository content" response for a single file
 * (GET /repos/{owner}/{repo}/contents/{path}). Lab 2 uses this to read
 * house_config.json before overwriting it, since a write requires the
 * file's current `sha` (GitHub's optimistic-concurrency check).
 */
data class FileContentDto(
    @SerializedName("content") val content: String,
    @SerializedName("encoding") val encoding: String,
    @SerializedName("sha") val sha: String,
    @SerializedName("path") val path: String
)
