package com.wakaapps.api

import com.wakaapps.api.dto.InsightResponse
import com.wakaapps.api.dto.ReviewRequest
import com.wakaapps.domain.InsightId
import com.wakaapps.repository.InsightsRepository
import io.micronaut.http.annotation.*

@Controller("/admin")
class ReviewAdminController(
    private val insightsRepository: InsightsRepository,
) {
    @Get("/review/today")
    fun today(): List<InsightResponse> =
        insightsRepository
            .listReviewDue()
            .map { InsightResponse.fromDomain(it) }

    @Post("/insights/{insightId}/review")
    fun review(
        @PathVariable insightId: String,
        @Body req: ReviewRequest,
    ): InsightResponse {
        val updated = insightsRepository.updateReviewResult(
            insightId = InsightId(insightId),
            result = req.result,
        )
        return InsightResponse.fromDomain(updated)
    }
}
