package com.wakaapps.config

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

@Factory
class DynamoDbFactory(
    private val config: DynamoDbConfig,
) {
    @Singleton
    fun dynamoDbClient(): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            .region(Region.of(config.region))

        val endpoint = config.endpoint?.trim()?.takeIf { it.isNotEmpty() }
        return if (endpoint != null) {
            // DynamoDB Local: endpoint override + dummy credentials
            builder
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("local", "local")
                    )
                )
                .build()
        } else {
            // AWS: use standard provider chain (env, profile, instance role)
            builder
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()
        }
    }
}
