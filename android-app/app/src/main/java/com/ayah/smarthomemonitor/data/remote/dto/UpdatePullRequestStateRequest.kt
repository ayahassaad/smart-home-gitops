package com.ayah.smarthomemonitor.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Request body for PATCH /repos/{owner}/{repo}/pulls/{pull_number} — used to close a PR. */
data class UpdatePullRequestStateRequest(
    @SerializedName("state") val state: String = "closed"
)
