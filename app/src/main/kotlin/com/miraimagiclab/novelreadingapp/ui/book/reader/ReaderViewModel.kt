package com.miraimagiclab.novelreadingapp.ui.book.reader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.content.ContentComponentRepository
import com.miraimagiclab.novelreadingapp.data.statistics.ReadingStatsUpdate
import com.miraimagiclab.novelreadingapp.data.statistics.StatsRepository
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.ContentViewModel
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.flip.FlipPageContentViewModel
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.scroll.ScrollContentViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository,
    val contentComponentRepository: ContentComponentRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private var contentViewModel: ContentViewModel by mutableStateOf(ContentViewModel.Companion.empty)
    private val _uiState = MutableReaderScreenUiState(contentViewModel.uiState)
    val uiState: ReaderScreenUiState = _uiState
    private val readingBookListUserData =
        userDataRepository.stringListUserData(UserDataPath.ReadingBooks.path)
    var bookId = ""
        set(value) {
            field = value
            _uiState.bookId = value
            contentViewModel.changeBookId(value)
            addToReadingBook(value)
            viewModelScope.launch(Dispatchers.IO) {
                statsRepository.updateReadingStatistics(
                    ReadingStatsUpdate(
                        bookId = value,
                        sessionDelta = 1
                    )
                )
                val readingData = bookRepository.getUserReadingData(value)
                statsRepository.updateBookStatus(
                    bookId = value,
                    isFirstReading = readingData.lastReadTime.year != 1971
                )
            }

            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookVolumesFlow(value, viewModelScope).collect { _uiState.bookVolumes = it }
            }
        }
    private var chapterId = ""
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        viewModelScope.launch {
            settingState.isUsingFlipPageUserData.getFlowWithDefault(false).collect {
                if (it && contentViewModel !is FlipPageContentViewModel) {
                    contentViewModel = FlipPageContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        updateReadingProgress = ::saveReadingProgress,
                        contentComponentRepository = contentComponentRepository
                    )
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
                }
                else if (!it && contentViewModel !is ScrollContentViewModel) {
                    contentViewModel = ScrollContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        settingState = settingState,
                        updateReadingProgress = ::saveReadingProgress,
                        contentComponentRepository = contentComponentRepository
                    )
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
                }
            }
        }
    }

    fun prevChapter() = contentViewModel.loadLastChapter()

    fun nextChapter() = contentViewModel.loadNextChapter()

    fun changeChapter(chapterId: String) {
        this.chapterId = chapterId
        contentViewModel.changeChapter(chapterId)
    }

    private fun saveReadingProgress(progress: Float) {
        if (_uiState.contentUiState.readingChapterContent.isEmpty()) return
        val chapterId = _uiState.contentUiState.readingChapterContent.id
        if (progress.isNaN() || progress == 0f || bookId.isBlank()) return
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

    fun updateTotalReadingTime(bookId: String, totalReadingTime: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) {
                it.apply {
                    lastReadTime = LocalDateTime.now()
                    totalReadTime = it.totalReadTime + totalReadingTime
                }
            }
        }
    }

    private fun addToReadingBook(bookId: String) {
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

    fun accumulateReadingTime(bookId: String, seconds: Int) {
        if (bookId.isBlank()) return
        coroutineScope.launch(Dispatchers.IO) {
            statsRepository.accumulateBookReadTime(bookId, seconds)
        }
    }

}