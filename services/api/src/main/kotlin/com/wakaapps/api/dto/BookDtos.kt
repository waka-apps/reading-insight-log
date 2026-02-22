package com.wakaapps.api.dto

import com.wakaapps.domain.Book
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import java.time.Instant

@Serdeable.Deserializable
data class CreateBookRequest(
    @field:NotBlank
    val title: String,
    val author: String? = null,
)

@Serdeable
data class BookResponse(
    val id: String,
    val title: String,
    val author: String?,
    val updatedAt: Instant,
    val insightCount: Int = 0,
) {
    companion object {
        fun from(
            book: Book,
            insightCount: Int,
        ): BookResponse =
            BookResponse(
                id = book.id.value,
                title = book.title,
                author = book.author,
                updatedAt = book.updatedAt,
                insightCount = insightCount,
            )
    }
}
