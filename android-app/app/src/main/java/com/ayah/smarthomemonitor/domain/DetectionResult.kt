package com.ayah.smarthomemonitor.domain

/**
 * Result of running [DeceptionDetector] against a single GitHub comment.
 *
 * @param isAttack true if [confidence] reached the detector's alert threshold.
 * @param confidence a score from 0-100 representing how confident the detector
 *        is that the comment is adversarial/deceptive text from LuxAgent.
 * @param matchedPatterns the specific keywords/phrases that were found in the
 *        comment, grouped as "category: keyword" strings — kept around purely
 *        so the UI/video walkthrough can show *why* a comment was flagged.
 */
data class DetectionResult(
    val isAttack: Boolean,
    val confidence: Int,
    val matchedPatterns: List<String>
)
