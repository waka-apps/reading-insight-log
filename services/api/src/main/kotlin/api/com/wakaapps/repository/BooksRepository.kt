package api.com.wakaapps.repository

import api.com.wakaapps.domain.Book
import api.com.wakaapps.domain.BookId
import jakarta.inject.Singleton
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.sortedByDescending

@Singleton
class BooksRepository {
    private val store = ConcurrentHashMap<String, Book>()

    fun create(title: String, author: String?): Book {
        val now = Instant.now()
        val book = Book(
            id = BookId(UUID.randomUUID().toString()),
            title = title,
            author = author,
            createdAt = now,
            updatedAt = now,
        )
        store[book.id.value] = book
        return book
    }

    fun list(): List<Book> =
        store.values.sortedByDescending { it.updatedAt }
}
