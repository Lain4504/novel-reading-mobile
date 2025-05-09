package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScrollContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val settingState: SettingState,
    val updateReadingProgress: (Float) -> Unit
) : ContentViewModel {
    private var lazyColumnSize = IntSize(0, 0)
    private var lastWriteReadingProgress = 0L

    override val uiState: MutableScrollContentUiSate = MutableScrollContentUiSate(
        loadLastChapter = ::loadLastChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        setLazyColumnSize = {
            lazyColumnSize = it
        }
    )

    init {
        coroutineScope.launch(Dispatchers.IO) {
            if (!settingState.isUsingContinuousScrollingUserData.getOrDefault(false)) return@launch
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0) }.collect { itemInfo ->
                if (uiState.lazyListState.layoutInfo.visibleItemsInfo.size == 1 &&
                    itemInfo?.key?.equals(uiState.readingChapterContent.lastChapter) == true &&
                    itemInfo.offset != 0
                ) {
                    uiState.contentList.removeAt(uiState.contentList.size - 1)
                    uiState.readingContentId = uiState.readingChapterContent.lastChapter
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                            lastReadChapterProgress =
                                if (it.lastReadChapterId == uiState.readingChapterContent.id) it.lastReadChapterProgress else 0f
                        }
                    }
                    if (!uiState.readingChapterContent.hasLastChapter()) return@collect
                    coroutineScope.launch(Dispatchers.IO) {
                        bookRepository.getChapterContent(
                            chapterId = uiState.readingChapterContent.lastChapter,
                            bookId = uiState.bookId
                        ).collect {
                            if (uiState.contentList[0].id == it.id) {
                                uiState.contentList.removeAt(0)
                            }
                            uiState.contentList.add(0, it)
                        }
                    }
                    return@collect
                }
                if (itemInfo?.key?.equals(uiState.readingChapterContent.nextChapter) == true) {
                    uiState.contentList.removeAt(0)
                    uiState.readingContentId = uiState.readingChapterContent.nextChapter
                    if (!uiState.readingChapterContent.hasNextChapter()) return@collect
                    coroutineScope.launch(Dispatchers.IO) {
                        bookRepository.getChapterContent(
                            chapterId = uiState.readingChapterContent.nextChapter,
                            bookId = uiState.bookId
                        ).collect {
                            if (uiState.contentList.last().id == it.id) {
                                uiState.contentList.removeAt(uiState.contentList.lastIndex)
                            }
                            uiState.contentList.add(it)
                        }
                    }
                    return@collect
                }
            }
        }
        coroutineScope.launch {
            snapshotFlow { uiState.lazyListState.firstVisibleItemScrollOffset }.collect { _ ->
                val item =
                    uiState.lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == uiState.readingChapterContent.id }
                if (item == null) return@collect
                uiState.readingProgress =
                    1f.coerceAtMost((-item.offset + lazyColumnSize.height).toFloat() / item.size)
                if (System.currentTimeMillis() - lastWriteReadingProgress < 250 && uiState.readingProgress < 1f) return@collect
                lastWriteReadingProgress = System.currentTimeMillis()
                updateReadingProgress(uiState.readingProgress)
            }
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
        uiState.contentList.clear()
        uiState.readingContentId = id
        coroutineScope.launch(Dispatchers.IO) {
            val chapterContent = bookRepository.getChapterContent(
                chapterId = id,
                bookId = uiState.bookId
            )
            chapterContent.collect { content ->
                if (content.id == -1) return@collect
                if (uiState.contentList.contains(content)) {
                    uiState.contentList.removeAll { it.id == content.id }
                }
                uiState.contentList.add(1.coerceAtMost(uiState.contentList.size), content)
                uiState.readingProgress = 0f
                bookRepository.updateUserReadingData(uiState.bookId) {
                    it.apply {
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = uiState.readingChapterContent.title
                        lastReadChapterProgress =
                            if (it.lastReadChapterId == id) it.lastReadChapterProgress else 0f
                    }
                }
                coroutineScope.launch(Dispatchers.IO) {
                    if (uiState.contentList.firstOrNull { it.id == id }
                            ?.hasLastChapter() == true) {
                        val lastChapterContentFlow = bookRepository.getChapterContent(
                            uiState.contentList.firstOrNull { it.id == id }!!.lastChapter,
                            uiState.bookId
                        )
                        if (settingState.isUsingContinuousScrollingUserData.getOrDefault(false))
                            lastChapterContentFlow
                                .collect { lastChapterContent ->
                                    if (uiState.contentList.contains(lastChapterContent)) {
                                        uiState.contentList.removeAll { it.id == lastChapterContent.id }
                                    }
                                    uiState.contentList.add(0, lastChapterContent)
                                }
                    }
                }
                coroutineScope.launch(Dispatchers.IO) {
                    if (uiState.contentList.firstOrNull { it.id == id }
                            ?.hasNextChapter() == true) {
                        val nextChapterContentFlow = bookRepository.getChapterContent(
                            uiState.contentList.firstOrNull { it.id == id }!!.nextChapter,
                            uiState.bookId
                        )
                        if (settingState.isUsingContinuousScrollingUserData.getOrDefault(false))
                            nextChapterContentFlow.collect { nextChapterContent ->
                                if (uiState.contentList.contains(nextChapterContent)) {
                                    uiState.contentList.removeAll { it.id == nextChapterContent.id }
                                }
                                uiState.contentList.add(
                                    uiState.contentList.size,
                                    nextChapterContent
                                )
                            }
                    }
                }
                coroutineScope.launch(Dispatchers.IO) {
                    val userReadingData = bookRepository.getUserReadingData(uiState.bookId)
                    coroutineScope.launch {
                        uiState.lazyListState.scrollToItem(uiState.contentList.indexOfFirst { it.id == id })
                        uiState.lazyListState.scrollToItem(
                            uiState.contentList.indexOfFirst { it.id == id },
                            if (userReadingData.lastReadChapterId == uiState.readingChapterContent.id)
                                (uiState.lazyListState.layoutInfo.visibleItemsInfo.first { it.key == id }.size * userReadingData.lastReadChapterProgress).toInt() - lazyColumnSize.height
                            else
                                0
                        )
                    }
                }
            }
        }
    }
}