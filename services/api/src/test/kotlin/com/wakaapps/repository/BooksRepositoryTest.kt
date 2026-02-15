package com.wakaapps.repository

import com.wakaapps.config.DynamoDbConfig
import com.wakaapps.domain.DailyBookLimitExceededException
import com.wakaapps.support.DynamoDbTestSupport
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "RUN_DYNAMODB_TESTS", matches = "true")
class BooksRepositoryTest : TestPropertyProvider {

    @Inject
    lateinit var dynamoDb: DynamoDbClient

    @Inject
    lateinit var config: DynamoDbConfig

    @Inject
    lateinit var booksRepository: BooksRepository

    @BeforeAll
    fun setupTable() {
        DynamoDbTestSupport.ensureTable(dynamoDb, config.tableName)
    }

    @BeforeEach
    fun clearTable() {
        DynamoDbTestSupport.clearTable(dynamoDb, config.tableName)
    }

    @Test
    fun createAndFind() {
        val created = booksRepository.create("Book A", "Author")
        val found = booksRepository.findById(created.id)
        assertNotNull(found)
        assertEquals("Book A", found?.title)
    }

    @Test
    fun dailyLimitIsEnforced() {
        booksRepository.create("Book A", null)
        booksRepository.create("Book B", null)
        assertThrows(DailyBookLimitExceededException::class.java) {
            booksRepository.create("Book C", null)
        }
    }

    override fun getProperties(): MutableMap<String, String> =
        mutableMapOf(
            "app.dynamodb.table-name" to "reading-insight-log-test",
            "app.dynamodb.endpoint" to "http://localhost:8000",
            "app.dynamodb.user-id" to "test",
            "app.dynamodb.daily-book-limit" to "2",
        )
}
