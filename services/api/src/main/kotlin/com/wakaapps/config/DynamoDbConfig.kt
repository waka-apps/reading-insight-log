package com.wakaapps.config

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("app.dynamodb")
class DynamoDbConfig {
    lateinit var tableName: String
    var endpoint: String? = null
    var region: String = "ap-northeast-1"
    var userId: String = "default"
    var dailyInsightLimit: Int = 25
    var dailyBookLimit: Int = 5
}
