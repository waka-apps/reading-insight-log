package com.wakaapps.repository

import com.wakaapps.config.DynamoDbConfig
import com.wakaapps.domain.BookId
import com.wakaapps.domain.Insight
import com.wakaapps.domain.InsightId
import com.wakaapps.domain.ReviewResult
import jakarta.inject.Singleton
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.Select
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import java.time.Instant

@Singleton
class InsightsRepository(
    private val dynamoDb: DynamoDbClient,
    private val config: DynamoDbConfig,
) {
    private val tableName = config.tableName
    private val userPk = "USER#${config.userId}"

    /**
     * 追加
     */
    fun add(
        bookId: BookId,
        quote: String,
        interpretation: String,
        tags: List<String>,
    ): Insight {
        val insight = Insight.createInitial(
            bookId = bookId,
            quote = quote,
            interpretation = interpretation,
            tags = tags,
        )

        val createdAtMillis = insight.createdAt.toEpochMilli()
        val item = mutableMapOf<String, AttributeValue>()
        item[PK] = s(userPk)
        item[SK] = s(insightSk(createdAtMillis, insight.id))
        item[TYPE] = s(TYPE_INSIGHT)
        item[INSIGHT_ID] = s(insight.id.value)
        item[BOOK_ID] = s(bookId.value)
        item[QUOTE] = s(insight.quote)
        item[INTERPRETATION] = s(insight.interpretation)
        item[TAGS] = AttributeValue.builder().l(tags.map { s(it.trim()) }.filter { it.s().isNotEmpty() }).build()
        item[CREATED_AT] = n(createdAtMillis)
        item[UPDATED_AT] = n(insight.updatedAt.toEpochMilli())
        item[NEXT_REVIEW_AT] = n(insight.nextReviewAt.toEpochMilli())
        item[REVIEW_INTERVAL_DAYS] = n(insight.reviewIntervalDays.toLong())
        item[GSI1PK] = s(gsi1Pk(bookId))
        item[GSI1SK] = s(gsi1Sk(createdAtMillis, insight.id))

        dynamoDb.putItem(
            PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
        )
        return insight
    }

    /**
     * 本ごとの一覧（createdAt 降順）
     */
    fun listByBook(bookId: BookId): List<Insight> {
        val items = queryInsightsByBook(bookId)
        return items.map { toInsight(it) }
    }

    /**
     * 復習期限到達（nextReviewAt <= now）
     * "today" を厳密に日付で切らず、v1方針どおり now 基準にする
     */
    fun listReviewDue(now: Instant = Instant.now()): List<Insight> {
        val items = scanReviewDue(now)
        return items
            .map { toInsight(it) }
            .sortedBy { it.nextReviewAt }
    }

    /**
     * 復習結果を適用して更新
     */
    fun updateReviewResult(
        insightId: InsightId,
        result: ReviewResult,
        now: Instant = Instant.now(),
    ): Insight {
        val currentItem = findInsightItemById(insightId)
            ?: throw NoSuchElementException("Insight not found: ${insightId.value}")

        val current = toInsight(currentItem)
        val updated = current.applyReview(result = result, now = now)

        dynamoDb.updateItem(
            UpdateItemRequest.builder()
                .tableName(tableName)
                .key(
                    mapOf(
                        PK to currentItem[PK]!!,
                        SK to currentItem[SK]!!,
                    )
                )
                .updateExpression("SET #nextReviewAt = :nextReviewAt, #reviewIntervalDays = :reviewIntervalDays")
                .expressionAttributeNames(
                    mapOf(
                        "#nextReviewAt" to NEXT_REVIEW_AT,
                        "#reviewIntervalDays" to REVIEW_INTERVAL_DAYS,
                    )
                )
                .expressionAttributeValues(
                    mapOf(
                        ":nextReviewAt" to n(updated.nextReviewAt.toEpochMilli()),
                        ":reviewIntervalDays" to n(updated.reviewIntervalDays.toLong()),
                    )
                )
                .build()
        )

        return updated
    }

    fun findById(insightId: InsightId): Insight? =
        findInsightItemById(insightId)?.let { toInsight(it) }

    fun countByBooks(bookIds: List<BookId>): Map<BookId, Int> {
        if (bookIds.isEmpty()) {
            return emptyMap()
        }

        val counts = mutableMapOf<BookId, Int>()
        for (bookId in bookIds) {
            val count = queryInsightCountByBook(bookId)
            counts[bookId] = count
        }
        return counts
    }

    private fun queryInsightsByBook(bookId: BookId): List<Map<String, AttributeValue>> {
        val items = mutableListOf<Map<String, AttributeValue>>()
        var lastKey: Map<String, AttributeValue>? = null

        do {
            val request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(GSI1)
                .keyConditionExpression("#gsi1pk = :pk AND begins_with(#gsi1sk, :skPrefix)")
                .expressionAttributeNames(
                    mapOf(
                        "#gsi1pk" to GSI1PK,
                        "#gsi1sk" to GSI1SK,
                    )
                )
                .expressionAttributeValues(
                    mapOf(
                        ":pk" to s(gsi1Pk(bookId)),
                        ":skPrefix" to s("INSIGHT#"),
                    )
                )
                .scanIndexForward(false)
                .exclusiveStartKey(lastKey)
                .build()

            val response = dynamoDb.query(request)
            items.addAll(response.items())
            lastKey = response.lastEvaluatedKey().takeIf { it.isNotEmpty() }
        } while (lastKey != null)

        return items
    }

    private fun queryInsightCountByBook(bookId: BookId): Int {
        var total = 0
        var lastKey: Map<String, AttributeValue>? = null

        do {
            val request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(GSI1)
                .keyConditionExpression("#gsi1pk = :pk AND begins_with(#gsi1sk, :skPrefix)")
                .expressionAttributeNames(
                    mapOf(
                        "#gsi1pk" to GSI1PK,
                        "#gsi1sk" to GSI1SK,
                    )
                )
                .expressionAttributeValues(
                    mapOf(
                        ":pk" to s(gsi1Pk(bookId)),
                        ":skPrefix" to s("INSIGHT#"),
                    )
                )
                .select(Select.COUNT)
                .exclusiveStartKey(lastKey)
                .build()

            val response = dynamoDb.query(request)
            total += response.count()
            lastKey = response.lastEvaluatedKey().takeIf { it.isNotEmpty() }
        } while (lastKey != null)

        return total
    }

    private fun scanReviewDue(now: Instant): List<Map<String, AttributeValue>> {
        val items = mutableListOf<Map<String, AttributeValue>>()
        var lastKey: Map<String, AttributeValue>? = null

        do {
            val request = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#pk = :pk AND #type = :type AND #nextReviewAt <= :now")
                .expressionAttributeNames(
                    mapOf(
                        "#pk" to PK,
                        "#type" to TYPE,
                        "#nextReviewAt" to NEXT_REVIEW_AT,
                    )
                )
                .expressionAttributeValues(
                    mapOf(
                        ":pk" to s(userPk),
                        ":type" to s(TYPE_INSIGHT),
                        ":now" to n(now.toEpochMilli()),
                    )
                )
                .exclusiveStartKey(lastKey)
                .build()

            val response = dynamoDb.scan(request)
            items.addAll(response.items())
            lastKey = response.lastEvaluatedKey().takeIf { it.isNotEmpty() }
        } while (lastKey != null)

        return items
    }

    private fun findInsightItemById(insightId: InsightId): Map<String, AttributeValue>? {
        val request = ScanRequest.builder()
            .tableName(tableName)
            .filterExpression("#pk = :pk AND #type = :type AND #insightId = :id")
            .expressionAttributeNames(
                mapOf(
                    "#pk" to PK,
                    "#type" to TYPE,
                    "#insightId" to INSIGHT_ID,
                )
            )
            .expressionAttributeValues(
                mapOf(
                    ":pk" to s(userPk),
                    ":type" to s(TYPE_INSIGHT),
                    ":id" to s(insightId.value),
                )
            )
            .limit(1)
            .build()

        val response = dynamoDb.scan(request)
        return response.items().firstOrNull()
    }

    private fun toInsight(item: Map<String, AttributeValue>): Insight {
        val id = InsightId(item[INSIGHT_ID]?.s() ?: error("Missing insightId"))
        val bookId = BookId(item[BOOK_ID]?.s() ?: error("Missing bookId"))
        val quote = item[QUOTE]?.s() ?: error("Missing quote")
        val interpretation = item[INTERPRETATION]?.s() ?: error("Missing interpretation")
        val tags = item[TAGS]?.l()?.mapNotNull { it.s() } ?: emptyList()
        val createdAt = Instant.ofEpochMilli(item[CREATED_AT]?.n()?.toLong() ?: 0L)
        val updatedAt = Instant.ofEpochMilli(item[UPDATED_AT]?.n()?.toLong() ?: createdAt.toEpochMilli())
        val nextReviewAt = Instant.ofEpochMilli(item[NEXT_REVIEW_AT]?.n()?.toLong() ?: createdAt.toEpochMilli())
        val reviewIntervalDays = item[REVIEW_INTERVAL_DAYS]?.n()?.toInt() ?: 1

        return Insight(
            id = id,
            bookId = bookId,
            quote = quote,
            interpretation = interpretation,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt,
            nextReviewAt = nextReviewAt,
            reviewIntervalDays = reviewIntervalDays,
        )
    }

    private fun insightSk(createdAtMillis: Long, insightId: InsightId): String =
        "INSIGHT#${createdAtMillis.toString().padStart(13, '0')}#${insightId.value}"

    private fun gsi1Pk(bookId: BookId): String =
        "USER#${config.userId}#BOOK#${bookId.value}"

    private fun gsi1Sk(createdAtMillis: Long, insightId: InsightId): String =
        "INSIGHT#${createdAtMillis.toString().padStart(13, '0')}#${insightId.value}"

    private fun s(value: String): AttributeValue =
        AttributeValue.builder().s(value).build()

    private fun n(value: Long): AttributeValue =
        AttributeValue.builder().n(value.toString()).build()

    private companion object {
        const val PK = "PK"
        const val SK = "SK"
        const val GSI1 = "GSI1"
        const val GSI1PK = "GSI1PK"
        const val GSI1SK = "GSI1SK"
        const val TYPE = "type"
        const val TYPE_INSIGHT = "INSIGHT"
        const val INSIGHT_ID = "insightId"
        const val BOOK_ID = "bookId"
        const val QUOTE = "quote"
        const val INTERPRETATION = "interpretation"
        const val TAGS = "tags"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val NEXT_REVIEW_AT = "nextReviewAt"
        const val REVIEW_INTERVAL_DAYS = "reviewIntervalDays"
    }
}
