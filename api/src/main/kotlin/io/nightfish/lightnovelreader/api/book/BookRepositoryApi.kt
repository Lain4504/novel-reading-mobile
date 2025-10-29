package io.nightfish.lightnovelreader.api.book

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface BookRepositoryApi {
    fun getStateBookInformation(id: Int, coroutineScope: CoroutineScope): BookInformation
    fun getBookInformationFlow(id: Int, coroutineScope: CoroutineScope): Flow<BookInformation>
    fun getBookVolumesFlow(id: Int, coroutineScope: CoroutineScope): Flow<BookVolumes>
    fun getStateChapterContent(
        chapterId: Int,
        bookId: Int,
        coroutineScope: CoroutineScope
    ): ChapterContent

    suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent
    fun getChapterContentFlow(
        chapterId: Int,
        bookId: Int,
        coroutineScope: CoroutineScope
    ): Flow<ChapterContent>

    fun getStateUserReadingData(bookId: Int, coroutineScope: CoroutineScope): UserReadingData
    fun getUserReadingData(bookId: Int): UserReadingData
    fun getUserReadingDataFlow(bookId: Int): Flow<UserReadingData>
    fun getAllUserReadingData(): List<UserReadingData>
    fun updateUserReadingData(id: Int, update: (MutableUserReadingData) -> UserReadingData)
    suspend fun getIsBookCached(bookId: Int): Boolean
    fun progressBookTagClick(tag: String, navController: NavController)
}