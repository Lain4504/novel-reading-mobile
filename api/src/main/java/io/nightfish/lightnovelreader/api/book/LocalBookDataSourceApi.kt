package io.nightfish.lightnovelreader.api.book

import kotlinx.coroutines.flow.Flow

interface LocalBookDataSourceApi {
    suspend fun getBookInformation(id: Int): BookInformation?
    fun updateBookInformation(info: BookInformation)
    suspend fun getBookVolumes(id: Int): BookVolumes?
    fun updateBookVolumes(bookId: Int, bookVolumes: BookVolumes)
    suspend fun getChapterContent(id: Int): MutableChapterContent?
    fun updateChapterContent(chapterContent: ChapterContent)
    fun getUserReadingData(id: Int): MutableUserReadingData
    fun getUserReadingDataFlow(id: Int): Flow<MutableUserReadingData>
    fun updateUserReadingData(id: Int, update: (MutableUserReadingData) -> UserReadingData)
    fun getAllUserReadingData(): List<UserReadingData>
    suspend fun isChapterContentExists(id: Int): Boolean
    fun clear()
}