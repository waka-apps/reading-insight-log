package com.wakaapps.api

import com.wakaapps.api.dto.BookResponse
import com.wakaapps.api.dto.CreateBookRequest
import com.wakaapps.repository.BooksRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.validation.Valid

@Validated
@Controller("/admin/books")
class BooksAdminController(
    private val booksRepository: BooksRepository
) {
    @Post(consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun create(@Body @Valid req: CreateBookRequest): HttpResponse<BookResponse> {
        val title = req.title.trim()
        val author = req.author?.trim()?.takeIf { it.isNotBlank() }
        val created = booksRepository.create(title, author)
        return HttpResponse.created(BookResponse.from(created))
    }

    @Get(produces = [MediaType.APPLICATION_JSON])
    fun list(): List<BookResponse> =
        booksRepository.list().map { BookResponse.from(it) }
}
