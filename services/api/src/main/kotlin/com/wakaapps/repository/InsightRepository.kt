package com.wakaapps.repository

import com.wakaapps.domain.BookId
import com.wakaapps.domain.Insight
import com.wakaapps.domain.InsightId
import com.wakaapps.domain.ReviewResult
import jakarta.inject.Singleton
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Singleton
class InsightsRepository {

    // InsightId.value -> Insight
    private val store = ConcurrentHashMap<String, Insight>()

    /**
     * 追加
     */
    fun add(
        bookId: BookId,
        quote: String,
        interpretation: String,
        tags: List<String>,
    ): Insight {
        val now = Instant.now()
        val id = InsightId(UUID.randomUUID().toString())

        val insight = Insight.createInitial(
            id = id,
            bookId = bookId,
            quote = quote,
            interpretation = interpretation,
            tags = tags,
            now = now,
        )

        store[id.value] = insight
        return insight
    }

    /**
     * 本ごとの一覧（createdAt 降順）
     */
    fun listByBook(bookId: BookId): List<Insight> =
        store.values
            .asSequence()
            .filter { it.bookId == bookId }
            .sortedByDescending { it.createdAt }
            .toList()

    /**
     * 復習期限到達（nextReviewAt <= now）
     * "today" を厳密に日付で切らず、v1方針どおり now 基準にする
     */
    fun listReviewDue(now: Instant = Instant.now()): List<Insight> =
        store.values
            .asSequence()
            .filter { it.nextReviewAt <= now }
            .sortedBy { it.nextReviewAt }
            .toList()

    /**
     * 復習結果を適用して更新
     */
    fun updateReviewResult(
        insightId: InsightId,
        result: ReviewResult,
        now: Instant = Instant.now(),
    ): Insight {
        val current = store[insightId.value]
            ?: throw NoSuchElementException("Insight not found: ${insightId.value}")

        val updated = current.applyReview(result = result, now = now)
        store[insightId.value] = updated
        return updated
    }

    fun findById(insightId: InsightId): Insight? =
        store[insightId.value]
}
