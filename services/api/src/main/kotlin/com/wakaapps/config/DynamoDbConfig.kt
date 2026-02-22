package com.wakaapps.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.dynamodb")
class DynamoDbConfig {
    lateinit var tableName: String
    var endpoint: String? = null
    var region: String = "ap-northeast-1"
    var userId: String = "default"
    var dailyInsightLimit: Int = DEFAULT_DAILY_INSIGHT_LIMIT
    var dailyBookLimit: Int = DEFAULT_DAILY_BOOK_LIMIT

    companion object {
        const val DEFAULT_DAILY_INSIGHT_LIMIT = 25
        const val DEFAULT_DAILY_BOOK_LIMIT = 5
    }
}
