package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.ReadingStatsUpdate
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private var contentViewModel: ContentViewModel by mutableStateOf(
        FlipPageContentViewModel(
            bookRepository = bookRepository,
            coroutineScope = viewModelScope,
            updateReadingProgress = ::saveReadingProgress
        )
    )
    private val _uiState = MutableReaderScreenUiState(contentViewModel.uiState)
    val uiState: ReaderScreenUiState = _uiState
    private val readingBookListUserData =
        userDataRepository.intListUserData(UserDataPath.ReadingBooks.path)
    var bookId = -1
        set(value) {
            field = value
            _uiState.bookId = value
            contentViewModel.changeBookId(value)
            addToReadingBook(value)
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookVolumes(value).collect {
                    _uiState.bookVolumes = it
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                statsRepository.updateReadingStatistics(
                    ReadingStatsUpdate(
                        bookId = bookId,
                        sessionDelta = 1
                    )
                )
                val readingData = bookRepository.getUserReadingData(bookId)
                if (readingData.lastReadTime.year < 1971)
                    coroutineScope.launch {
                        statsRepository.updateBookStatus(
                            bookId = bookId,
                            isFirstReading = true
                        )
                    }
            }
        }
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            settingState.isUsingFlipPageUserData.getFlow().collectLatest { isFlipEnabled ->
                val useFlip = isFlipEnabled == true
                val currentChapterId = uiState.contentUiState.readingChapterContent.id

                if (useFlip && contentViewModel !is FlipPageContentViewModel) {
                    val newContentViewModel = FlipPageContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        updateReadingProgress = ::saveReadingProgress
                    )
                    newContentViewModel.changeBookId(bookId)
                    newContentViewModel.changeChapter(currentChapterId)
                    contentViewModel = newContentViewModel
                    _uiState.contentUiState = newContentViewModel.uiState
                } else if (!useFlip && contentViewModel !is ScrollContentViewModel) {
                    val newViewModel = ScrollContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        settingState = settingState,
                        updateReadingProgress = ::saveReadingProgress
                    )
                    newViewModel.changeBookId(bookId)
                    newViewModel.changeChapter(currentChapterId)
                    contentViewModel = newViewModel
                    _uiState.contentUiState = newViewModel.uiState
                }
            }
        }
    }

    fun lastChapter() = contentViewModel.loadLastChapter()

    fun nextChapter() = contentViewModel.loadNextChapter()

    fun changeChapter(chapterId: Int) {
        contentViewModel.changeChapter(chapterId)
    }

    private fun saveReadingProgress(progress: Float) {
        if (_uiState.contentUiState.readingChapterContent.isEmpty()) return
        val chapterId = _uiState.contentUiState.readingChapterContent.id
        if (progress.isNaN() || progress == 0f || bookId == -1) return
        Log.v("ReaderViewModel", "$bookId/$chapterId Saving progress $progress. (${_uiState.contentUiState.readingChapterContent.title})")
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = LocalDateTime.now()

            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                val isChapterCompleted = progress > 0.945 &&
                        !userReadingData.readCompletedChapterIds.contains(chapterId)

                userReadingData.apply {
                    lastReadTime = currentTime
                    lastReadChapterId = chapterId
                    lastReadChapterProgress = progress.coerceIn(0f..1f)
                    val totalChapters = _uiState.bookVolumes.volumes.sumOf { it.chapters.size }
                    readingProgress = if (totalChapters == 0) {
                        readingProgress
                    } else if (isChapterCompleted) {
                        (userReadingData.readCompletedChapterIds.size + 1) / totalChapters.toFloat()
                    } else {
                        userReadingData.readCompletedChapterIds.size / totalChapters.toFloat()
                    }
                    if (isChapterCompleted)
                        readCompletedChapterIds.add(chapterId)
                }
            }
        }
    }

    fun updateTotalReadingTime(bookId: Int, totalReadingTime: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) {
                it.apply {
                    lastReadTime = LocalDateTime.now()
                    totalReadTime = it.totalReadTime + totalReadingTime
                }
            }
        }
    }

    private fun addToReadingBook(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBookListUserData.update {
                val newList = it.toMutableList()
                if (it.contains(bookId))
                    newList.remove(bookId)
                newList.add(bookId)
                return@update newList
            }
        }
    }

    fun accumulateReadingTime(bookId: Int, seconds: Int) {
        if (bookId == -1) return
        coroutineScope.launch(Dispatchers.IO) {
            statsRepository.accumulateBookReadTime(bookId, seconds)
        }
    }
}