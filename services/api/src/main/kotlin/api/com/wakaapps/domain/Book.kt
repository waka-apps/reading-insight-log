package api.com.wakaapps.domain

import java.time.Instant

data class Book(
    val id: BookId,
    val title: String,
    val author: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
