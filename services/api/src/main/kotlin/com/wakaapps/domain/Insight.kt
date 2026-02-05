package com.wakaapps.domain

import java.time.Instant

data class Insight(
    val id: InsightId,
    val bookId: BookId,
    val quote: String,
    val interpretation: String,
    val tags: List<String>,
    val createdAt: Instant,
    val nextReviewAt: Instant,
    val reviewIntervalDays: Int,
)