package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(FlowPreview::class)
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
        }
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        contentViewModel.changeBookId(bookId)
        viewModelScope.launch {
            settingState.isUsingFlipPageUserData.getFlow().collect {
                if (it == true && contentViewModel !is FlipPageContentViewModel) {
                    contentViewModel = FlipPageContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        updateReadingProgress = ::saveReadingProgress
                    )
                    val chapterId = uiState.contentUiState.readingChapterContent.id
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
                }
                else if (contentViewModel !is ScrollContentViewModel) {
                    contentViewModel = ScrollContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        settingState = settingState,
                        updateReadingProgress = ::saveReadingProgress
                    )
                    val chapterId = uiState.contentUiState.readingChapterContent.id
                    contentViewModel.changeBookId(bookId)
                    contentViewModel.changeChapter(chapterId)
                    _uiState.contentUiState = contentViewModel.uiState
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
        viewModelScope.launch(Dispatchers.IO) {
            if (progress.isNaN()) return@launch
            if (_uiState.contentUiState.readingChapterContent.isEmpty()) return@launch
            if (bookId == -1) return@launch
            val chapterId = _uiState.contentUiState.readingChapterContent.id
            val currentTime = LocalDateTime.now()

            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                val isChapterCompleted = progress > 0.945 &&
                        !userReadingData.readCompletedChapterIds.contains(chapterId)

                userReadingData.apply {
                    lastReadTime = currentTime
                    lastReadChapterId = chapterId
                    lastReadChapterProgress = progress.coerceIn(0f..1f)
                    readingProgress = if (isChapterCompleted) {
                        (userReadingData.readCompletedChapterIds.size + 1) /
                                _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
                    } else {
                        userReadingData.readCompletedChapterIds.size /
                                _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
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