package com.wakaapps.domain

class DailyInsightLimitExceededException(
    val limit: Int,
    val dateKey: String,
) : RuntimeException("Daily insight limit exceeded: $limit for date $dateKey")
