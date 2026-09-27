package com.ayah.smarthomemonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayah.smarthomemonitor.data.network.NetworkModule
import com.ayah.smarthomemonitor.data.repository.SmartHomeRepository
import com.ayah.smarthomemonitor.domain.DeceptionDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Owns the monitor screen's UI state and the background GitHub-polling loop.
 *
 * MVVM boundary: this class calls [SmartHomeRepository] (never Retrofit
 * directly) and [DeceptionDetector] (never inlines detection logic itself).
 * The Compose View only ever reads [uiState] / [actionInProgress] and calls
 * [onForceMerge] / [onForceReject].
 */
class MonitorViewModel(
    private val repository: SmartHomeRepository = SmartHomeRepository(NetworkModule.gitHubApiService),
    private val detector: DeceptionDetector = DeceptionDetector(),
    private val pollIntervalMillis: Long = 30_000L
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Normal)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // True while a Force Merge / Force Reject write is in flight, so the UI
    // can disable the buttons and avoid firing the same GitHub write twice.
    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()

    // Comment IDs we've already scored, so the same comment isn't
    // re-analyzed (and re-alerted on) every 30-second poll. In-memory only —
    // no database needed for Lab 1.
    private val processedCommentIds = mutableSetOf<Long>()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                pollOnce()
                delay(pollIntervalMillis)
            }
        }
    }

    /** One full poll cycle: fetch open PRs + comments, score any new ones. */
    private suspend fun pollOnce() {
        try {
            val prsWithComments = repository.getOpenPullRequestsWithComments()

            // Lab 2: close the GitOps loop. If we're currently showing an
            // alert for a PR that is no longer open (the operator, or
            // anyone, closed or merged it directly on GitHub), reset back to
            // Normal automatically — the operator shouldn't have to
            // manually clear the app after resolving it in git.
            val currentState = _uiState.value
            if (currentState is UiState.SecurityAlert) {
                val alertedPrStillOpen = prsWithComments.any {
                    it.pullRequest.number == currentState.pullNumber
                }
                if (!alertedPrStillOpen) {
                    _uiState.value = UiState.Normal
                    return
                }
            }

            for (prWithComments in prsWithComments) {
                for (comment in prWithComments.comments) {
                    if (processedCommentIds.contains(comment.id)) continue
                    processedCommentIds.add(comment.id)

                    // Detection runs on Dispatchers.Default: it's CPU-bound
                    // regex work, not I/O, so it gets its own dispatcher
                    // rather than piggy-backing on the IO one.
                    val result = withContext(Dispatchers.Default) {
                        detector.analyze(comment.body)
                    }

                    if (result.isAttack) {
                        _uiState.value = UiState.SecurityAlert(
                            confidence = result.confidence,
                            text = comment.body,
                            pullNumber = prWithComments.pullRequest.number
                        )
                        // Stop scanning further comments this cycle — we've
                        // already surfaced an alert to the operator.
                        return
                    }
                }
            }
        } catch (e: IOException) {
            // Network failure (offline, DNS, timeout, etc). Don't crash;
            // surface it and keep polling on the next cycle.
            _uiState.value = UiState.Error("Could not reach GitHub: ${e.message}")
        } catch (e: retrofit2.HttpException) {
            // GitHub responded with a non-2xx (bad token, rate limit, 404
            // repo, etc). Same treatment — visible, not fatal.
            _uiState.value = UiState.Error("GitHub API error: HTTP ${e.code()}")
        }
    }

    // --- Lab 2: operator decisions ---------------------------------------

    /**
     * Operator decides the flagged comment is a lie and approves the
     * EcoAgent's original energy-saving proposal anyway: writes the
     * approved config (17.0°C, Android-Operator) to `main` and closes the PR.
     */
    fun onForceMerge() = handleOperatorAction { pullNumber -> repository.forceMerge(pullNumber) }

    /** Operator agrees the flagged comment is a genuine attack: denies the change, just closes the PR. */
    fun onForceReject() = handleOperatorAction { pullNumber -> repository.forceReject(pullNumber) }

    private fun handleOperatorAction(action: suspend (pullNumber: Int) -> Result<Unit>) {
        val current = _uiState.value
        // Buttons are only meaningful while an alert is showing, and only
        // one write should be in flight at a time — ignore extra taps.
        if (current !is UiState.SecurityAlert) return
        if (_actionInProgress.value) return

        val pullNumber = current.pullNumber
        viewModelScope.launch(Dispatchers.IO) {
            _actionInProgress.value = true
            val result = action(pullNumber)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Normal
                },
                onFailure = { e ->
                    _uiState.value = UiState.Error(describeActionFailure(e))
                }
            )
            _actionInProgress.value = false
        }
    }

    /**
     * Defensive fault tolerance for Lab 2's write calls: translate whatever
     * GitHub / the network threw into a message an operator can act on,
     * without ever crashing the app.
     */
    private fun describeActionFailure(e: Throwable): String = when (e) {
        is retrofit2.HttpException -> when (e.code()) {
            401 -> "GitHub rejected the token as unauthorized — check the token in local.properties."
            403 -> "GitHub rejected the request — rate limited, or the token lacks write permission."
            404 -> "Pull Request or file not found on GitHub — it may already be closed."
            405 -> "GitHub could not merge this Pull Request (not mergeable — check for conflicts)."
            409 -> "The file changed on GitHub since this app last read it. Please try again."
            422 -> "GitHub rejected the request as unprocessable — check branch protection settings."
            else -> "GitHub API error: HTTP ${e.code()}"
        }
        is IOException -> "Could not reach GitHub: ${e.message}"
        else -> "Unexpected error: ${e.message}"
    }
}
