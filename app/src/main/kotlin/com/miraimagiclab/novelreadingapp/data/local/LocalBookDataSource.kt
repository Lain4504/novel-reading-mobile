package com.miraimagiclab.novelreadingapp.data.local

import com.miraimagiclab.novelreadingapp.data.local.room.dao.BookInformationDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.BookVolumesDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.ChapterContentDao
import com.miraimagiclab.novelreadingapp.data.local.room.dao.UserReadingDataDao
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.book.BookVolumes
import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.book.LocalBookDataSourceApi
import io.lain4504.novelreadingapp.api.book.MutableChapterContent
import io.lain4504.novelreadingapp.api.book.MutableUserReadingData
import io.lain4504.novelreadingapp.api.book.UserReadingData
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao,
    private val userReadingDataDao: UserReadingDataDao
): LocalBookDataSourceApi {
    override suspend fun getBookInformation(id: String): BookInformation? = bookInformationDao.get(id)
    override fun updateBookInformation(info: BookInformation) = bookInformationDao.update(info)
    override suspend fun getBookVolumes(id: String): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    override fun updateBookVolumes(bookId: String, bookVolumes: BookVolumes) =
        bookVolumesDao.update(bookId, bookVolumes)

    override suspend fun getChapterContent(id: String) = chapterContentDao.get(id)?.let {
        MutableChapterContent(
            it.id,
            it.title,
            it.content,
            it.lastChapter,
            it.nextChapter
        )
    }
    override fun updateChapterContent(chapterContent: ChapterContent) =
        chapterContentDao.update(chapterContent)

    override fun getUserReadingData(id: String) = userReadingDataDao.getEntity(id).let {
        it ?: return@let MutableUserReadingData.empty().apply { this.id = id }
        MutableUserReadingData(
            it.id,
            it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle,
            it.lastReadChapterProgress,
            it.readCompletedChapterIds
        )
    }

    override fun getUserReadingDataFlow(id: String) = userReadingDataDao.getEntityFlow(id).map {
        it ?: return@map MutableUserReadingData.empty().apply { this.id = id }
        MutableUserReadingData(
            it.id,
            it.lastReadTime,
            it.totalReadTime,
            it.readingProgress,
            it.lastReadChapterId,
            it.lastReadChapterTitle,
            it.lastReadChapterProgress,
            it.readCompletedChapterIds
        )
    }

    override fun updateUserReadingData(id: String, update: (MutableUserReadingData) -> UserReadingData) {
        val userReadingData = userReadingDataDao.getEntityWithoutFlow(id)?.let {
            MutableUserReadingData(
                it.id,
                it.lastReadTime,
                it.totalReadTime,
                it.readingProgress,
                it.lastReadChapterId,
                it.lastReadChapterTitle,
                it.lastReadChapterProgress,
                it.readCompletedChapterIds
            )
        } ?: MutableUserReadingData.empty().apply { this.id = id }
        userReadingDataDao.update(update(userReadingData.apply { this.id = id }).let {
            var data = it.toMutable()
            if (it.readingProgress.isNaN()) data = data.apply { this.readingProgress = 0.0f }
            if (it.lastReadChapterProgress.isNaN()) data =
                data.apply { this.lastReadChapterProgress = 0.0f }
            return@let data
        })
    }

    override fun getAllUserReadingData(): List<UserReadingData> =
        userReadingDataDao.getAll().map {
            MutableUserReadingData(
                it.id,
                it.lastReadTime,
                it.totalReadTime,
                it.readingProgress,
                it.lastReadChapterId,
                it.lastReadChapterTitle,
                it.lastReadChapterProgress,
                it.readCompletedChapterIds
            )
        }

    override suspend fun isChapterContentExists(id: String): Boolean =
        chapterContentDao.getId(id) != null

    override fun clear() {
        userReadingDataDao.clear()
        bookInformationDao.clear()
        bookVolumesDao.clear()
        chapterContentDao.clear()
    }
}