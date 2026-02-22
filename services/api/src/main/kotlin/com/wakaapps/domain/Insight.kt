package com.wakaapps.domain

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Insight(
    val id: InsightId,
    val bookId: BookId,
    val quote: String,
    val interpretation: String,
    val tags: List<String>,
    val nextReviewAt: Instant,
    val reviewIntervalDays: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun createInitial(
            bookId: BookId,
            quote: String,
            interpretation: String,
            tags: List<String>,
        ): Insight {
            val now = Instant.now()
            val id = InsightId(UUID.randomUUID().toString())
            val interval = 1
            return Insight(
                id = id,
                bookId = bookId,
                quote = quote.trim(),
                interpretation = interpretation.trim(),
                tags = tags.map { it.trim() }.filter { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now,
                reviewIntervalDays = interval,
                nextReviewAt = now.plus(interval.toLong(), ChronoUnit.DAYS),
            )
        }
    }

    fun applyReview(
        result: ReviewResult,
        now: Instant,
    ): Insight {
        val nextInterval =
            when (result) {
                ReviewResult.REMEMBERED -> reviewIntervalDays * 2
                ReviewResult.UNCERTAIN -> 1
            }
        return copy(
            reviewIntervalDays = nextInterval,
            nextReviewAt = now.plus(nextInterval.toLong(), ChronoUnit.DAYS),
        )
    }
}
