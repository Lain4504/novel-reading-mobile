package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.json.BookUserData
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.work.CacheBookWork
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.book.MutableUserReadingData
import io.nightfish.lightnovelreader.api.book.UserReadingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val localBookDataSource: LocalBookDataSource,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository,
    private val workManager: WorkManager
): BookRepositoryApi {
    override fun getStateBookInformation(id: Int, coroutineScope: CoroutineScope): BookInformation =
        textProcessingRepository.processBookInformation {
            val bookInformation = MutableBookInformation.Companion.empty()
            bookInformation.id = id
            coroutineScope.launch(Dispatchers.IO) {
                localBookDataSource.getBookInformation(id)?.let(bookInformation::update)
                webBookDataSourceProvider.value.getBookInformation(id).let {
                    if (it.isEmpty()) return@launch
                    localBookDataSource.updateBookInformation(it)
                    bookInformation.update(textProcessingRepository.processBookInformation { it })
                }
            }
            return@processBookInformation bookInformation
        }

    override fun getBookInformationFlow(id: Int, coroutineScope: CoroutineScope): Flow<BookInformation> {
        val bookInformation: MutableStateFlow<BookInformation> =
            MutableStateFlow(BookInformation.Companion.empty(id))
        coroutineScope.launch(Dispatchers.IO) {
            bookInformation.update {
                localBookDataSource.getBookInformation(id) ?: BookInformation.Companion.empty(id)
            }
            webBookDataSourceProvider.value.getBookInformation(id).let { information ->
                localBookDataSource.updateBookInformation(information)
                localBookDataSource.getBookInformation(id)?.let { newInfo ->
                    bookInformation.update { newInfo }
                    bookshelfRepository.getBookshelfBookMetadata(information.id)
                        ?.let { bookshelfBookMetadata ->
                            if (bookshelfBookMetadata.lastUpdate.isBefore(information.lastUpdated))
                                bookshelfBookMetadata.bookShelfIds.forEach {
                                    bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(
                                        information.id,
                                        information.lastUpdated
                                    )
                                    bookshelfRepository.addUpdatedBooksIntoBookShelf(it, id)
                                }
                        }
                }
            }
        }

        return bookInformation.map {
            textProcessingRepository.processBookInformation { it }
        }
    }

    override fun getBookVolumesFlow(id: Int, coroutineScope: CoroutineScope): Flow<BookVolumes> {
        val bookVolumes: MutableStateFlow<BookVolumes> =
            MutableStateFlow(BookVolumes.Companion.empty(id))

        coroutineScope.launch(Dispatchers.IO) {
            bookVolumes.update {
                localBookDataSource.getBookVolumes(id) ?: BookVolumes.Companion.empty(id)
            }
            webBookDataSourceProvider.value.getBookVolumes(id).let { newBookVolumes ->
                localBookDataSource.updateBookVolumes(id, newBookVolumes)
                bookVolumes.update {
                    newBookVolumes
                }
            }
        }

        return bookVolumes.map {
            textProcessingRepository.processBookVolumes { it }
        }
    }

    override fun getStateChapterContent(
        chapterId: Int,
        bookId: Int,
        coroutineScope: CoroutineScope
    ): ChapterContent =
        textProcessingRepository.processChapterContent(bookId) {
            val chapterContent = MutableChapterContent.empty()
            chapterContent.id = chapterId
            coroutineScope.launch(Dispatchers.IO) {
                localBookDataSource.getChapterContent(chapterId)?.let {
                    if (it.isEmpty()) return@launch
                    chapterContent.update(
                        it.toMutable().apply {
                            this.content = textProcessingRepository
                                .processChapterContent(bookId) { this }
                                .content
                        })
                }
                webBookDataSourceProvider.value.getChapterContent(chapterId, bookId).let {
                    if (it.isEmpty()) return@launch
                    localBookDataSource.updateChapterContent(it)
                    chapterContent.update(
                        it.toMutable().apply {
                            this.content = textProcessingRepository
                                .processChapterContent(bookId) { this }
                                .content
                        })
                }
            }
            return@processChapterContent chapterContent
        }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent =
        withContext(Dispatchers.IO) {
            textProcessingRepository.coroutineProcessChapterContent(bookId) {
                val webChapterContent = webBookDataSourceProvider.value.getChapterContent(chapterId, bookId)
                if (!webChapterContent.isEmpty()) {
                    localBookDataSource.updateChapterContent(webChapterContent)
                    return@coroutineProcessChapterContent webChapterContent
                }
                return@coroutineProcessChapterContent localBookDataSource.getChapterContent(
                    chapterId
                )
                    ?: MutableChapterContent.Companion.empty().apply { id = chapterId }
            }
        }

    override fun getChapterContentFlow(
        chapterId: Int,
        bookId: Int,
        coroutineScope: CoroutineScope
    ): Flow<ChapterContent> {
        val chapterContent: MutableStateFlow<ChapterContent> =
            MutableStateFlow(
                ChapterContent.Companion.empty().toMutable().apply { id = chapterId }
            )
        coroutineScope.launch(Dispatchers.IO) {
            chapterContent.update {
                localBookDataSource.getChapterContent(chapterId) ?: MutableChapterContent.Companion.empty()
                    .apply { id = chapterId }
            }
            webBookDataSourceProvider.value.getChapterContent(
                chapterId = chapterId,
                bookId = bookId
            ).let { content ->
                if (content.isEmpty()) return@launch
                localBookDataSource.updateChapterContent(content)
                localBookDataSource.getChapterContent(chapterId)?.let { newContent ->
                    chapterContent.update {
                        newContent.toMutable().apply {
                            lastChapter =
                                if (newContent.lastChapter == -1) it.lastChapter else newContent.lastChapter
                            nextChapter =
                                if (newContent.nextChapter == -1) it.nextChapter else newContent.nextChapter
                        }
                    }
                }
            }
        }
        return chapterContent.map {
            textProcessingRepository.processChapterContent(bookId) { it }
        }
    }

    override fun getStateUserReadingData(bookId: Int, coroutineScope: CoroutineScope): UserReadingData {
        val userReadingData = MutableUserReadingData.Companion.empty()
        userReadingData.id = bookId
        coroutineScope.launch(Dispatchers.IO) {
            localBookDataSource.getUserReadingData(bookId).let(userReadingData::update)
        }
        return userReadingData
    }

    override fun getUserReadingData(bookId: Int): UserReadingData =
        localBookDataSource.getUserReadingData(bookId)

    override fun getUserReadingDataFlow(bookId: Int): Flow<UserReadingData> =
        localBookDataSource.getUserReadingDataFlow(bookId)

    override fun getAllUserReadingData(): List<UserReadingData> =
        localBookDataSource.getAllUserReadingData()

    override fun updateUserReadingData(id: Int, update: (MutableUserReadingData) -> UserReadingData) {
        localBookDataSource.updateUserReadingData(id, update)
    }

    fun isCacheBookWorkFlow(workId: UUID) = workManager.getWorkInfoByIdFlow(workId)

    fun importUserReadingData(data: AppUserDataContent): Boolean {
        val userReadingDataList: List<BookUserData> = data.bookUserData ?: return false
        userReadingDataList.forEach { bookUserData ->
            localBookDataSource.updateUserReadingData(bookUserData.id) {
                MutableUserReadingData(
                    id = bookUserData.id,
                    lastReadTime = if (bookUserData.lastReadTime.isAfter(it.lastReadTime)) bookUserData.lastReadTime else it.lastReadTime,
                    totalReadTime = if (bookUserData.totalReadTime > it.totalReadTime) bookUserData.totalReadTime else it.totalReadTime,
                    readingProgress = if (bookUserData.readingProgress > it.readingProgress) bookUserData.readingProgress else it.readingProgress,
                    lastReadChapterId = bookUserData.lastReadChapterId,
                    lastReadChapterTitle = bookUserData.lastReadChapterTitle,
                    lastReadChapterProgress = bookUserData.lastReadChapterProgress,
                    readCompletedChapterIds = (bookUserData.readCompletedChapterIds + it.readCompletedChapterIds).distinct()
                )
            }
        }
        return true
    }

    fun cacheBook(bookId: Int): OneTimeWorkRequest {
        val workRequest = OneTimeWorkRequestBuilder<CacheBookWork>()
            .setInputData(
                workDataOf(
                    "bookId" to bookId
                )
            )
            .build()
        workManager.enqueueUniqueWork(
            bookId.toString(),
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        return workRequest
    }

    override suspend fun getIsBookCached(bookId: Int): Boolean {
        localBookDataSource.getBookVolumes(bookId)?.let { bookVolumes ->
            if (bookVolumes.volumes.isEmpty())
                return false
            bookVolumes.volumes.forEach { bookVolume ->
                bookVolume.chapters.forEach {
                    if (!localBookDataSource.isChapterContentExists(it.id))
                        return false
                }
            }
        } ?: return false
        return true
    }

    override fun progressBookTagClick(tag: String, navController: NavController) =
        webBookDataSourceProvider.value.progressBookTagClick(tag, navController)
}