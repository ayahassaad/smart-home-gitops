package com.ayah.smarthomemonitor.viewmodel

/**
 * The three states the monitor screen can be in. A sealed class so the
 * Compose UI can exhaustively `when` over it.
 */
sealed class UiState {

    /** No active attack detected. Green screen. */
    data object Normal : UiState()

    /**
     * A comment scored at/above the detector's threshold. Red screen.
     * [pullNumber] identifies which open PR triggered the alert, so the
     * operator's Force Merge / Force Reject decision (Lab 2) is applied to
     * the right PR, and so polling can tell when that specific PR has been
     * resolved (closed or merged) and reset the screen back to Normal.
     */
    data class SecurityAlert(
        val confidence: Int,
        val text: String,
        val pullNumber: Int
    ) : UiState()

    /** GitHub could not be reached / an API call failed. The app keeps
     * polling in the background; this is surfaced so it's visible during
     * the demo/grading rather than silently swallowed. */
    data class Error(
        val message: String
    ) : UiState()
}
