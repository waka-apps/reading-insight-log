package com.wakaapps.api

import com.wakaapps.api.dto.BookResponse
import com.wakaapps.api.dto.CreateBookRequest
import com.wakaapps.domain.DailyBookLimitExceededException
import com.wakaapps.repository.BooksRepository
import com.wakaapps.repository.InsightsRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.validation.Validated
import jakarta.validation.Valid

@Validated
@Controller("/admin/books")
class BooksAdminController(
    private val booksRepository: BooksRepository,
    private val insightsRepository: InsightsRepository,
) {
    @Post(consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun create(@Body @Valid req: CreateBookRequest): HttpResponse<BookResponse> {
        val title = req.title.trim()
        val author = req.author?.trim()?.takeIf { it.isNotBlank() }
        val created = try {
            booksRepository.create(title, author)
        } catch (e: DailyBookLimitExceededException) {
            throw HttpStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Daily book limit exceeded: ${e.limit} (${e.dateKey})"
            )
        }
        return HttpResponse.created(BookResponse.from(created, 0))
    }

    @Get(produces = [MediaType.APPLICATION_JSON])
    fun list(): List<BookResponse> {
        val books = booksRepository.list()
        val countsByBooks = insightsRepository.countByBooks(books.map { it.id })
        return books.map { book ->
            val insightCount = countsByBooks[book.id] ?: 0
            BookResponse.from(book, insightCount)
        }
    }
}
