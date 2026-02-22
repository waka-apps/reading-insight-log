package com.wakaapps.api

import com.wakaapps.api.dto.InsightResponse
import com.wakaapps.api.dto.ReviewRequest
import com.wakaapps.domain.InsightId
import com.wakaapps.repository.InsightsRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.exceptions.HttpStatusException
import java.time.Instant

@Controller("/admin")
class ReviewAdminController(
    private val insightsRepository: InsightsRepository,
) {
    @Get("/review/today")
    fun today(
        @QueryValue("now") now: String?,
    ): List<InsightResponse> {
        val baseNow =
            now
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    runCatching { Instant.parse(it) }
                        .getOrElse {
                            throw HttpStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid 'now'. Use ISO-8601 like 2026-02-11T10:00:00Z",
                            )
                        }
                }
                ?: Instant.now()

        return insightsRepository
            .listReviewDue(baseNow)
            .map { InsightResponse.fromDomain(it) }
    }

    @Post("/insights/{insightId}/review")
    fun review(
        @PathVariable insightId: String,
        @Body req: ReviewRequest,
    ): InsightResponse {
        val updated =
            insightsRepository.updateReviewResult(
                insightId = InsightId(insightId),
                result = req.result,
            )
        return InsightResponse.fromDomain(updated)
    }
}
