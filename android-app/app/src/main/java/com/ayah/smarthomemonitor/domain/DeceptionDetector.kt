package com.ayah.smarthomemonitor.domain

/**
 * Pure Kotlin, framework-free detector for LuxAgent-style adversarial PR
 * comments.
 *
 * Deliberately simple by design (per the Lab 1 spec): no ML, no LLM calls, no
 * external services. It is a weighted-keyword/regex scorer:
 *
 *  1. Five categories of deceptive language are each backed by a list of
 *     regex patterns (see [CATEGORIES] below).
 *  2. For each category, if *any* pattern in that category matches the
 *     comment text, that category's fixed weight is added to the score once
 *     (multiple matches within the same category do not stack — this keeps
 *     the math easy to explain on camera: "5 categories, 5 weights, sum
 *     them up").
 *  3. The summed score is capped at 100 to produce a confidence percentage.
 *  4. If confidence >= [ATTACK_THRESHOLD], the comment is flagged as an
 *     attack.
 *
 * This class has zero Android dependencies, so it can be unit tested as
 * plain JVM code (see DeceptionDetectorTest).
 */
class DeceptionDetector {

    /** A single deceptive-language category: a name, its weight, and the
     * regex patterns that trigger it. */
    private data class Category(
        val name: String,
        val weight: Int,
        val patterns: List<Regex>
    )

    private val categories: List<Category> = listOf(
        Category(
            name = "HVAC/Valve failure",
            weight = 30,
            patterns = listOf(
                "valve failure", "valve malfunction", "stuck valve", "failed valve",
                "hvac failure", "hvac malfunction", "heating failure", "cooling failure",
                "compressor failure", "compressor blowout", "compression blowout",
                "pressure failure", "\\bvalve\\b", "\\bhvac\\b", "\\bcompressor\\b",
                "\\bblowout\\b"
            ).map { Regex(it, RegexOption.IGNORE_CASE) }
        ),
        Category(
            name = "Electrical fault",
            weight = 25,
            patterns = listOf(
                "electrical failure", "electrical fault", "short circuit", "shortcircuit",
                "wiring failure", "power failure", "electrical hazard", "electrical issue",
                "electrical emergency"
            ).map { Regex(it, RegexOption.IGNORE_CASE) }
        ),
        Category(
            name = "Structural damage",
            weight = 25,
            patterns = listOf(
                "structural crack", "structural failure", "structural damage",
                "cracked pipe", "burst pipe", "dangerous crack", "building damage"
            ).map { Regex(it, RegexOption.IGNORE_CASE) }
        ),
        Category(
            name = "Freezing danger",
            weight = 20,
            patterns = listOf(
                "freezing", "\\bfreeze\\b", "\\bfrozen\\b", "below freezing",
                "dangerously cold", "temperature emergency"
            ).map { Regex(it, RegexOption.IGNORE_CASE) }
        ),
        Category(
            name = "Urgency/deception language",
            weight = 25,
            patterns = listOf(
                "\\bcritical\\b", "\\bemergency\\b", "\\bimminent\\b", "\\bimmediately\\b",
                "do not lower", "\\bunsafe\\b", "\\bdanger\\b", "\\burgent\\b"
            ).map { Regex(it, RegexOption.IGNORE_CASE) }
        )
    )

    fun analyze(commentText: String): DetectionResult {
        val matched = mutableListOf<String>()
        var score = 0

        for (category in categories) {
            val hit = category.patterns.firstNotNullOfOrNull { pattern ->
                pattern.find(commentText)?.value
            }
            if (hit != null) {
                score += category.weight
                matched += "${category.name}: \"$hit\""
            }
        }

        val confidence = score.coerceAtMost(100)
        return DetectionResult(
            isAttack = confidence >= ATTACK_THRESHOLD,
            confidence = confidence,
            matchedPatterns = matched
        )
    }

    companion object {
        /** Confidence score (0-100) at/above which a comment is treated as an attack. */
        const val ATTACK_THRESHOLD = 50
    }
}
