package com.wakaapps.repository

import com.wakaapps.config.DynamoDbConfig
import com.wakaapps.domain.Book
import com.wakaapps.domain.BookId
import com.wakaapps.domain.DailyBookLimitExceededException
import jakarta.inject.Singleton
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import software.amazon.awssdk.services.dynamodb.model.Update
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Singleton
class BooksRepository(
    private val dynamoDb: DynamoDbClient,
    private val config: DynamoDbConfig,
) {
    private val tableName = config.tableName
    private val userPk = "USER#${config.userId}"

    fun create(
        title: String,
        author: String?,
    ): Book {
        val book =
            Book.createInitial(
                title = title,
                author = author,
            )

        val limit = config.dailyBookLimit
        val dateKey =
            LocalDate
                .ofInstant(book.createdAt, ZoneOffset.UTC)
                .format(DateTimeFormatter.BASIC_ISO_DATE)
        val item = mutableMapOf<String, AttributeValue>()
        item[PK] = s(userPk)
        item[SK] = s(bookSk(book.id))
        item[TYPE] = s(TYPE_BOOK)
        item[BOOK_ID] = s(book.id.value)
        item[TITLE] = s(book.title)
        author?.let { item[AUTHOR] = s(it) }
        item[CREATED_AT] = n(book.createdAt.toEpochMilli())
        item[UPDATED_AT] = n(book.updatedAt.toEpochMilli())

        try {
            dynamoDb.transactWriteItems(
                TransactWriteItemsRequest
                    .builder()
                    .transactItems(
                        TransactWriteItem
                            .builder()
                            .update(
                                Update
                                    .builder()
                                    .tableName(tableName)
                                    .key(
                                        mapOf(
                                            PK to s(userPk),
                                            SK to s(counterSk(dateKey)),
                                        ),
                                    ).updateExpression(COUNTER_UPDATE_EXPRESSION)
                                    .conditionExpression(COUNTER_LIMIT_CONDITION)
                                    .expressionAttributeNames(
                                        mapOf(
                                            "#count" to COUNTER_VALUE,
                                            "#date" to COUNTER_DATE,
                                            "#type" to TYPE,
                                        ),
                                    ).expressionAttributeValues(
                                        mapOf(
                                            ":zero" to n(0),
                                            ":inc" to n(1),
                                            ":limit" to n(limit.toLong()),
                                            ":date" to s(dateKey),
                                            ":type" to s(TYPE_COUNTER),
                                        ),
                                    ).build(),
                            ).build(),
                        TransactWriteItem
                            .builder()
                            .put(
                                Put
                                    .builder()
                                    .tableName(tableName)
                                    .item(item)
                                    .conditionExpression("attribute_not_exists(#pk) AND attribute_not_exists(#sk)")
                                    .expressionAttributeNames(
                                        mapOf(
                                            "#pk" to PK,
                                            "#sk" to SK,
                                        ),
                                    ).build(),
                            ).build(),
                    ).build(),
            )
        } catch (e: ConditionalCheckFailedException) {
            throw DailyBookLimitExceededException(limit = limit, dateKey = dateKey, cause = e)
        } catch (e: TransactionCanceledException) {
            throw DailyBookLimitExceededException(limit = limit, dateKey = dateKey, cause = e)
        }
        return book
    }

    fun list(): List<Book> {
        val items = queryAllBooks()
        return items
            .map { toBook(it) }
            .sortedByDescending { it.updatedAt }
    }

    fun findById(id: BookId): Book? {
        val response =
            dynamoDb.getItem(
                GetItemRequest
                    .builder()
                    .tableName(tableName)
                    .key(
                        mapOf(
                            PK to s(userPk),
                            SK to s(bookSk(id)),
                        ),
                    ).build(),
            )
        val item = response.item()
        if (item == null || item.isEmpty()) {
            return null
        }
        return toBook(item)
    }

    private fun queryAllBooks(): List<Map<String, AttributeValue>> {
        val items = mutableListOf<Map<String, AttributeValue>>()
        var lastKey: Map<String, AttributeValue>? = null

        do {
            val request =
                QueryRequest
                    .builder()
                    .tableName(tableName)
                    .keyConditionExpression("#pk = :pk AND begins_with(#sk, :skPrefix)")
                    .expressionAttributeNames(
                        mapOf(
                            "#pk" to PK,
                            "#sk" to SK,
                        ),
                    ).expressionAttributeValues(
                        mapOf(
                            ":pk" to s(userPk),
                            ":skPrefix" to s("BOOK#"),
                        ),
                    ).exclusiveStartKey(lastKey)
                    .build()

            val response = dynamoDb.query(request)
            items.addAll(response.items())
            lastKey = response.lastEvaluatedKey().takeIf { it.isNotEmpty() }
        } while (lastKey != null)

        return items
    }

    private fun toBook(item: Map<String, AttributeValue>): Book {
        val id = BookId(item[BOOK_ID]?.s() ?: error("Missing bookId"))
        val title = item[TITLE]?.s() ?: error("Missing title")
        val author = item[AUTHOR]?.s()
        val createdAt = Instant.ofEpochMilli(item[CREATED_AT]?.n()?.toLong() ?: 0L)
        val updatedAt = Instant.ofEpochMilli(item[UPDATED_AT]?.n()?.toLong() ?: createdAt.toEpochMilli())
        return Book(
            id = id,
            title = title,
            author = author,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun bookSk(bookId: BookId): String = "BOOK#${bookId.value}"

    private fun counterSk(dateKey: String): String = "COUNTER#DAILY_BOOK#$dateKey"

    private fun s(value: String): AttributeValue = AttributeValue.builder().s(value).build()

    private fun n(value: Long): AttributeValue = AttributeValue.builder().n(value.toString()).build()

    private companion object {
        const val PK = "PK"
        const val SK = "SK"
        const val TYPE = "type"
        const val TYPE_BOOK = "BOOK"
        const val TYPE_COUNTER = "COUNTER"
        const val COUNTER_UPDATE_EXPRESSION =
            "SET #count = if_not_exists(#count, :zero) + :inc, #date = :date, #type = :type"
        const val COUNTER_LIMIT_CONDITION = "attribute_not_exists(#count) OR #count < :limit"
        const val BOOK_ID = "bookId"
        const val TITLE = "title"
        const val AUTHOR = "author"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val COUNTER_VALUE = "counterValue"
        const val COUNTER_DATE = "counterDate"
    }
}
