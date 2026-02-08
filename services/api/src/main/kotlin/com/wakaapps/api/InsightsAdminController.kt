package com.wakaapps.api

import com.wakaapps.api.dto.CreateInsightRequest
import com.wakaapps.api.dto.InsightResponse
import com.wakaapps.domain.BookId
import com.wakaapps.repository.InsightsRepository
import io.micronaut.http.annotation.*
import jakarta.validation.Valid

@Controller("/admin")
class InsightsAdminController(
    private val insightsRepository: InsightsRepository,
) {
    @Post("/books/{bookId}/insights")
    fun addInsight(
        @PathVariable bookId: String,
        @Body @Valid req: CreateInsightRequest,
    ): InsightResponse {
        val insight = insightsRepository.add(
            bookId = BookId(bookId),
            quote = req.quote,
            interpretation = req.interpretation,
            tags = req.tags ?: emptyList(),
        )
        return InsightResponse.fromDomain(insight)
    }

    @Get("/books/{bookId}/insights")
    fun listByBook(
        @PathVariable bookId: String,
    ): List<InsightResponse> =
        insightsRepository
            .listByBook(BookId(bookId))
            .map { InsightResponse.fromDomain(it) }
}
