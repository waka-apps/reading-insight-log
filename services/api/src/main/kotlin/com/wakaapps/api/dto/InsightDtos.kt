package com.wakaapps.api.dto

import com.wakaapps.domain.Insight
import com.wakaapps.domain.ReviewResult
import io.micronaut.serde.annotation.Serdeable
import java.time.Instant

@Serdeable
data class CreateInsightRequest(
    val quote: String,
    val interpretation: String,
    val tags: List<String>? = null,
)

@Serdeable
data class InsightResponse(
    val id: String,
    val bookId: String,
    val quote: String,
    val interpretation: String,
    val tags: List<String>,
    val createdAt: Instant,
    val nextReviewAt: Instant,
    val reviewIntervalDays: Int,
) {
    companion object {
        fun fromDomain(insight: Insight): InsightResponse =
            InsightResponse(
                id = insight.id.value,
                bookId = insight.bookId.value,
                quote = insight.quote,
                interpretation = insight.interpretation,
                tags = insight.tags,
                createdAt = insight.createdAt,
                nextReviewAt = insight.nextReviewAt,
                reviewIntervalDays = insight.reviewIntervalDays,
            )
    }
}

@Serdeable
data class ReviewRequest(
    val result: ReviewResult,
)
