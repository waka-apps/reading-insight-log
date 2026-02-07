package com.wakaapps.domain

import java.time.Instant
import java.util.*

data class Book(
    val id: BookId,
    val title: String,
    val author: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun createInitial(
            title: String,
            author: String?,
        ): Book {
            val id = BookId(UUID.randomUUID().toString())
            val now = Instant.now()

            return Book(
                id = id,
                title = title,
                author = author,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
