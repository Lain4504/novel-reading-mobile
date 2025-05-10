package indi.dmzz_yyhyy.lightnovelreader.data.local

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableUserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookDataSource @Inject constructor(
    private val bookInformationDao: BookInformationDao,
    private val bookVolumesDao: BookVolumesDao,
    private val chapterContentDao: ChapterContentDao,
    private val userReadingDataDao: UserReadingDataDao
) {
    suspend fun getBookInformation(id: Int): BookInformation? = bookInformationDao.get(id)
    fun updateBookInformation(info: BookInformation) = bookInformationDao.update(info)
    suspend fun getBookVolumes(id: Int): BookVolumes? = bookVolumesDao.getBookVolumes(id)
    fun updateBookVolumes(bookId: Int, bookVolumes: BookVolumes) =
        bookVolumesDao.update(bookId, bookVolumes)

    suspend fun getChapterContent(id: Int) = chapterContentDao.get(id)?.let {
        MutableChapterContent(
            it.id,
            it.title,
            it.content,
            it.lastChapter,
            it.nextChapter
        )
    }
    fun updateChapterContent(chapterContent: ChapterContent) =
        chapterContentDao.update(chapterContent)

    fun getUserReadingData(id: Int) = userReadingDataDao.getEntity(id).let {
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

    fun getUserReadingDataFlow(id: Int) = userReadingDataDao.getEntityFlow(id).map {
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

    fun updateUserReadingData(id: Int, update: (MutableUserReadingData) -> UserReadingData) {
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

    fun getAllUserReadingData(): List<UserReadingData> =
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

    suspend fun isChapterContentExists(id: Int): Boolean =
        chapterContentDao.getId(id) != null

    fun clear() {
        userReadingDataDao.clear()
        bookInformationDao.clear()
        bookVolumesDao.clear()
        chapterContentDao.clear()
    }
}