package io.lain4504.novelreadingapp.api.book

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface BookRepositoryApi {
    fun getStateBookInformation(id: String, coroutineScope: CoroutineScope): BookInformation
    fun getBookInformationFlow(id: String, coroutineScope: CoroutineScope): Flow<BookInformation>
    fun getBookVolumesFlow(id: String, coroutineScope: CoroutineScope): Flow<BookVolumes>
    fun getStateChapterContent(
        chapterId: String,
        bookId: String,
        coroutineScope: CoroutineScope
    ): ChapterContent

    suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent
    fun getChapterContentFlow(
        chapterId: String,
        bookId: String,
        coroutineScope: CoroutineScope
    ): Flow<ChapterContent>

    fun getStateUserReadingData(bookId: String, coroutineScope: CoroutineScope): UserReadingData
    fun getUserReadingData(bookId: String): UserReadingData
    fun getUserReadingDataFlow(bookId: String): Flow<UserReadingData>
    fun getAllUserReadingData(): List<UserReadingData>
    fun updateUserReadingData(id: String, update: (MutableUserReadingData) -> UserReadingData)
    suspend fun getIsBookCached(bookId: String): Boolean
    fun progressBookTagClick(tag: String, navController: NavController)
}