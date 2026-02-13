package com.wakaapps.repository

import com.wakaapps.config.DynamoDbConfig
import com.wakaapps.domain.BookId
import com.wakaapps.domain.DailyInsightLimitExceededException
import com.wakaapps.support.DynamoDbTestSupport
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "RUN_DYNAMODB_TESTS", matches = "true")
class InsightsRepositoryTest : TestPropertyProvider {

    @Inject
    lateinit var dynamoDb: DynamoDbClient

    @Inject
    lateinit var config: DynamoDbConfig

    @Inject
    lateinit var insightsRepository: InsightsRepository

    @BeforeAll
    fun setupTable() {
        DynamoDbTestSupport.ensureTable(dynamoDb, config.tableName)
    }

    @BeforeEach
    fun clearTable() {
        DynamoDbTestSupport.clearTable(dynamoDb, config.tableName)
    }

    @Test
    fun addAndListByBook() {
        val bookId = BookId("book-1")
        insightsRepository.add(bookId, "q1", "i1", listOf("t1"))
        insightsRepository.add(bookId, "q2", "i2", listOf("t2"))
        val list = insightsRepository.listByBook(bookId)
        assertEquals(2, list.size)
        assertEquals("q2", list.first().quote)
    }

    @Test
    fun dailyLimitIsEnforced() {
        val bookId = BookId("book-2")
        insightsRepository.add(bookId, "q1", "i1", emptyList())
        insightsRepository.add(bookId, "q2", "i2", emptyList())
        assertThrows(DailyInsightLimitExceededException::class.java) {
            insightsRepository.add(bookId, "q3", "i3", emptyList())
        }
    }

    @Test
    fun reviewDueReturnsResults() {
        val bookId = BookId("book-3")
        val insight = insightsRepository.add(bookId, "q", "i", emptyList())
        val due = insightsRepository.listReviewDue(insight.nextReviewAt.plusSeconds(1))
        assertFalse(due.isEmpty())
    }

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "app.dynamodb.table-name" to "reading-insight-log-test",
            "app.dynamodb.endpoint" to "http://localhost:8000",
            "app.dynamodb.user-id" to "test",
            "app.dynamodb.daily-insight-limit" to "2",
        )
}
