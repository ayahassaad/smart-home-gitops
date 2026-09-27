package com.ayah.smarthomemonitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Minimal shape of a GitHub "Pull Request" object, as returned by
 * GET /repos/{owner}/{repo}/pulls. We only keep the fields Lab 1 needs.
 */
data class PullRequestDto(
    @SerializedName("number") val number: Int,
    @SerializedName("title") val title: String,
    @SerializedName("state") val state: String
)
