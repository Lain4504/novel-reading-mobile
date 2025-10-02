package io.nightfish.lightnovelreader.api.bookshelf

import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime

interface BookshelfRepositoryApi {
    fun getAllBookshelfIds(): List<Int>
    fun deleteBookshelf(bookshelfId: Int)
    fun addBookIntoBookShelf(bookshelfId: Int, bookInformation: BookInformation)
    fun addUpdatedBooksIntoBookShelf(bookShelfId: Int, bookId: Int)
    fun getAllBookshelfBookIdsFlow(): Flow<List<Int>>
    fun deleteBookFromBookshelf(bookshelfId: Int, bookId: Int)
    fun deleteBookFromBookshelfUpdatedBookIds(bookshelfId: Int, bookId: Int)
    fun updateBookshelfBookMetadataLastUpdateTime(bookId: Int, time: LocalDateTime)
    fun clear()
    fun getAllBookshelvesFlow(): Flow<List<MutableBookshelf>>
    fun getBookshelf(id: Int): MutableBookshelf?
    fun getBookshelfFlow(id: Int): Flow<MutableBookshelf?>
    fun createBookShelf(
        id: Int = Instant.now().epochSecond.hashCode(),
        name: String,
        sortType: BookshelfSortType,
        autoCache: Boolean,
        systemUpdateReminder: Boolean
    ): Int

    fun updateBookshelf(bookshelfId: Int, updater: (MutableBookshelf) -> Bookshelf)
    fun getAllBookshelfBooksMetadata(): List<BookshelfBookMetadata>
    fun getBookshelfBookMetadata(id: Int): BookshelfBookMetadata?
    fun getBookshelfBookMetadataFlow(id: Int): Flow<BookshelfBookMetadata?>
}