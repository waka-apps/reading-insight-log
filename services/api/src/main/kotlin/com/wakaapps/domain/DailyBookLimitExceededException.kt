package com.wakaapps.domain

class DailyBookLimitExceededException(
    val limit: Int,
    val dateKey: String,
    cause: Throwable? = null,
) : RuntimeException("Daily book limit exceeded: $limit for date $dateKey", cause)
