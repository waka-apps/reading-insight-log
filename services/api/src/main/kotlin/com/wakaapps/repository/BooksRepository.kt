package com.wakaapps.repository

import com.wakaapps.domain.Book
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class BooksRepository {
    private val store = ConcurrentHashMap<String, Book>()

    fun create(title: String, author: String?): Book {
        val book = Book.createInitial(
            title = title,
            author = author
        )

        store[book.id.value] = book
        return book
    }

    fun list(): List<Book> =
        store.values.sortedByDescending { it.updatedAt }
}
