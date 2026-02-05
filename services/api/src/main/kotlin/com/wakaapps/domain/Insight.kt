package com.wakaapps.domain

import java.time.Instant
import java.time.temporal.ChronoUnit

data class Insight(
    val id: InsightId,
    val bookId: BookId,
    val quote: String,
    val interpretation: String,
    val tags: List<String>,
    val createdAt: Instant,
    val nextReviewAt: Instant,
    val reviewIntervalDays: Int,
) {
    companion object {
        fun createInitial(
            id: InsightId,
            bookId: BookId,
            quote: String,
            interpretation: String,
            tags: List<String>,
            now: Instant,
        ): Insight {
            val interval = 1
            return Insight(
                id = id,
                bookId = bookId,
                quote = quote,
                interpretation = interpretation,
                tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
                createdAt = now,
                reviewIntervalDays = interval,
                nextReviewAt = now.plus(interval.toLong(), ChronoUnit.DAYS),
            )
        }
    }

    fun applyReview(result: ReviewResult, now: Instant): Insight {
        val nextInterval = when (result) {
            ReviewResult.REMEMBERED -> reviewIntervalDays * 2
            ReviewResult.UNCERTAIN -> 1
        }
        return copy(
            reviewIntervalDays = nextInterval,
            nextReviewAt = now.plus(nextInterval.toLong(), ChronoUnit.DAYS),
        )
    }
}
