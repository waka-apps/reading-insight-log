package com.wakaapps.repository

import com.wakaapps.domain.Book
import com.wakaapps.domain.BookId
import jakarta.inject.Singleton
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.sortedByDescending

@Singleton
class BooksRepository {
    private val store = ConcurrentHashMap<String, Book>()

    fun create(title: String, author: String?): Book {
        val book = Book.createBook(
            title = title,
            author = author)

        store[book.id.value] = book
        return book
    }

    fun list(): List<Book> =
        store.values.sortedByDescending { it.updatedAt }
}
