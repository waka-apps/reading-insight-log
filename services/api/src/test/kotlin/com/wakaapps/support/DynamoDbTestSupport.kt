package com.wakaapps.support

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement
import software.amazon.awssdk.services.dynamodb.model.KeyType
import software.amazon.awssdk.services.dynamodb.model.Projection
import software.amazon.awssdk.services.dynamodb.model.ProjectionType
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import software.amazon.awssdk.services.dynamodb.model.ScanRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest

import java.time.Duration

object DynamoDbTestSupport {
    private const val PK = "PK"
    private const val SK = "SK"
    private const val GSI1 = "GSI1"
    private const val GSI1PK = "GSI1PK"
    private const val GSI1SK = "GSI1SK"

    fun ensureTable(client: DynamoDbClient, tableName: String) {
        try {
            client.createTable(
                CreateTableRequest.builder()
                    .tableName(tableName)
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(
                        AttributeDefinition.builder().attributeName(PK).attributeType("S").build(),
                        AttributeDefinition.builder().attributeName(SK).attributeType("S").build(),
                        AttributeDefinition.builder().attributeName(GSI1PK).attributeType("S").build(),
                        AttributeDefinition.builder().attributeName(GSI1SK).attributeType("S").build(),
                    )
                    .keySchema(
                        KeySchemaElement.builder().attributeName(PK).keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName(SK).keyType(KeyType.RANGE).build(),
                    )
                    .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                            .indexName(GSI1)
                            .keySchema(
                                KeySchemaElement.builder().attributeName(GSI1PK).keyType(KeyType.HASH).build(),
                                KeySchemaElement.builder().attributeName(GSI1SK).keyType(KeyType.RANGE).build(),
                            )
                            .projection(
                                Projection.builder()
                                    .projectionType(ProjectionType.ALL)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        } catch (_: ResourceInUseException) {
            // already exists
        }

        waitForActive(client, tableName)
    }

    fun clearTable(client: DynamoDbClient, tableName: String) {
        var lastKey: Map<String, AttributeValue>? = null
        do {
            val response = client.scan(
                ScanRequest.builder()
                    .tableName(tableName)
                    .exclusiveStartKey(lastKey)
                    .projectionExpression("#pk, #sk")
                    .expressionAttributeNames(
                        mapOf(
                            "#pk" to PK,
                            "#sk" to SK,
                        )
                    )
                    .build()
            )

            val deletes = response.items().map { item ->
                val key = mapOf(
                    PK to item[PK]!!,
                    SK to item[SK]!!,
                )
                WriteRequest.builder()
                    .deleteRequest(DeleteRequest.builder().key(key).build())
                    .build()
            }

            deletes.chunked(25).forEach { batch ->
                client.batchWriteItem(
                    BatchWriteItemRequest.builder()
                        .requestItems(mapOf<String, List<WriteRequest>>(tableName to batch))
                        .build()
                )
            }

            lastKey = response.lastEvaluatedKey().takeIf { it.isNotEmpty() }
        } while (lastKey != null)
    }

    private fun waitForActive(client: DynamoDbClient, tableName: String) {
        val deadline = System.currentTimeMillis() + Duration.ofSeconds(10).toMillis()
        while (System.currentTimeMillis() < deadline) {
            val status = client.describeTable(
                DescribeTableRequest.builder().tableName(tableName).build()
            ).table().tableStatusAsString()
            if (status == "ACTIVE") {
                return
            }
            Thread.sleep(200)
        }
        throw AssertionError("DynamoDB table is not ACTIVE: $tableName")
    }
}
