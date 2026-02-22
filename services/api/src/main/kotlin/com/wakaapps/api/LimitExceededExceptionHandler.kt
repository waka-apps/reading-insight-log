package com.wakaapps.api

import com.wakaapps.domain.DailyBookLimitExceededException
import com.wakaapps.domain.DailyInsightLimitExceededException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

typealias DailyBookLimitHandler = ExceptionHandler<DailyBookLimitExceededException, HttpResponse<ErrorResponse>>
typealias DailyInsightLimitHandler = ExceptionHandler<DailyInsightLimitExceededException, HttpResponse<ErrorResponse>>

@Singleton
class DailyBookLimitExceededExceptionHandler : DailyBookLimitHandler {
    override fun handle(
        request: HttpRequest<*>,
        exception: DailyBookLimitExceededException,
    ): HttpResponse<ErrorResponse> =
        HttpResponse
            .status<ErrorResponse>(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse("Daily book limit exceeded: ${exception.limit} (${exception.dateKey})"))
}

@Singleton
class DailyInsightLimitExceededExceptionHandler : DailyInsightLimitHandler {
    override fun handle(
        request: HttpRequest<*>,
        exception: DailyInsightLimitExceededException,
    ): HttpResponse<ErrorResponse> =
        HttpResponse
            .status<ErrorResponse>(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse("Daily insight limit exceeded: ${exception.limit} (${exception.dateKey})"))
}

data class ErrorResponse(
    val message: String,
)
