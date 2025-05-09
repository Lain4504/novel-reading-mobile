package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class FlipPageContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val updateReadingProgress: (Float) -> Unit
) : ContentViewModel {
    private var notRecoveredProgress = 0f
    private var collectProgressJob: Job? = null
    override val uiState: MutableFlipPageContentUiState = MutableFlipPageContentUiState(
        loadLastChapter = ::loadLastChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        updatePageState = ::updatePagerState
    )

    init {
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.pagerState }.collect { pagerState ->
                println(uiState.pagerState.pageCount)
                collectProgressJob?.cancel()
                collectProgressJob = coroutineScope.launch(Dispatchers.IO) {
                    snapshotFlow { pagerState.settledPage }.collect {
                        uiState.readingProgress = (it + 1) / pagerState.pageCount.toFloat()
                        updateReadingProgress(uiState.readingProgress)
                    }
                }
            }
        }
    }

    fun updatePagerState(pagerState: PagerState) {
        uiState.pagerState = pagerState
        if (pagerState.pageCount == 0) return
        if (notRecoveredProgress <= 0) return
        notRecoveredProgress = 0f
        coroutineScope.launch {
            uiState.pagerState.scrollToPage((pagerState.pageCount * notRecoveredProgress).toInt() - 1)
        }
    }

    override fun changeBookId(id: Int) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        if (!uiState.readingChapterContent.hasNextChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.nextChapter
            )
        }
    }

    override fun loadLastChapter() {
        if (!uiState.readingChapterContent.hasLastChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.lastChapter
            )
        }
    }

    override fun changeChapter(id: Int) {
        notRecoveredProgress = 0f
        coroutineScope.launch(Dispatchers.IO) {
            val chapterContent = bookRepository.getChapterContent(
                chapterId = id,
                bookId = uiState.bookId
            )
            chapterContent.collect { content ->
                if (content.id == -1) return@collect
                uiState.readingChapterContent = content
                bookRepository.updateUserReadingData(uiState.bookId) {
                    it.apply {
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = uiState.readingChapterContent.title
                        lastReadChapterProgress =
                            if (it.lastReadChapterId == id) it.lastReadChapterProgress else 0f
                    }
                }
                if (content.hasNextChapter()) {
                    bookRepository.getChapterContent(
                        chapterId = id,
                        bookId = uiState.bookId
                    )
                }
            }
            bookRepository.getUserReadingDataFlow(uiState.bookId).collect {
                if (it.lastReadChapterId == uiState.readingChapterContent.id)
                    notRecoveredProgress = it.lastReadChapterProgress
            }
        }
    }
}