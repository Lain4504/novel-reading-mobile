package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.throttleLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private var contentViewModel: ContentViewModel by mutableStateOf(
        if (settingState.isUsingFlipPageUserData.get() == true) FlipPageContentViewModel(
            bookRepository = bookRepository,
            coroutineScope = viewModelScope,
            updateReadingProgress = ::saveReadingProgress
        )
        else ScrollContentViewModel(
            bookRepository = bookRepository,
            coroutineScope = viewModelScope,
            settingState = settingState,
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
            contentViewModel.changeBookId(value)
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookVolumes(value).collect {
                    _uiState.bookVolumes = it
                }
            }
        }
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        contentViewModel.changeBookId(bookId)
        viewModelScope.launch(Dispatchers.IO) {
            settingState.isUsingFlipPageUserData.getFlow().collect {
                if (it == true) FlipPageContentViewModel(
                    bookRepository = bookRepository,
                    coroutineScope = viewModelScope,
                    updateReadingProgress = ::saveReadingProgress
                )
                else ScrollContentViewModel(
                    bookRepository = bookRepository,
                    coroutineScope = viewModelScope,
                    updateReadingProgress = ::saveReadingProgress,
                    settingState = settingState
                )
                contentViewModel.changeBookId(bookId)
            }
        }
        viewModelScope.launch {
            settingState.isUsingFlipPageUserData.getFlow().collect {
                if (it == true && contentViewModel !is FlipPageContentViewModel)
                    contentViewModel = FlipPageContentViewModel(
                        bookRepository = bookRepository,
                        coroutineScope = viewModelScope,
                        updateReadingProgress = ::saveReadingProgress
                    )
                else if (contentViewModel !is ScrollContentViewModel) ScrollContentViewModel(
                    bookRepository = bookRepository,
                    coroutineScope = viewModelScope,
                    settingState = settingState,
                    updateReadingProgress = ::saveReadingProgress
                )
            }
        }
    }

    fun lastChapter() = contentViewModel.loadLastChapter()

    fun nextChapter() = contentViewModel.loadNextChapter()

    fun changeChapter(chapterId: Int) = contentViewModel.changeChapter(chapterId)

    private val _readingProgressChannel = Channel<Float>(Channel.CONFLATED)

    init {
        coroutineScope.launch(Dispatchers.IO) {
            val progressFlow = _readingProgressChannel.receiveAsFlow()
            merge(
                progressFlow.throttleLatest(1000),
                progressFlow.debounce(200)
            )
                .distinctUntilChanged()
                .collectLatest { progress ->
                    saveReadingProgress(progress)
                }
        }
    }

    private fun saveReadingProgress(progress: Float) {
        if (progress.isNaN()) return
        val chapterId = _uiState.contentUiState.readingChapterContent.id
        val currentTime = LocalDateTime.now()

        bookRepository.updateUserReadingData(bookId) { userReadingData ->
            val isChapterCompleted = progress > 0.945 &&
                    !userReadingData.readCompletedChapterIds.contains(chapterId)

            userReadingData.copy(
                lastReadTime = currentTime,
                lastReadChapterId = chapterId,
                lastReadChapterProgress = progress.coerceIn(0f..1f),
                readingProgress = if (isChapterCompleted) {
                    (userReadingData.readCompletedChapterIds.size + 1) /
                            _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
                } else {
                    userReadingData.readCompletedChapterIds.size /
                            _uiState.bookVolumes.volumes.sumOf { it.chapters.size }.toFloat()
                },
                readCompletedChapterIds = if (isChapterCompleted) {
                    userReadingData.readCompletedChapterIds + chapterId
                } else {
                    userReadingData.readCompletedChapterIds
                }
            )
        }
    }

    fun updateTotalReadingTime(bookId: Int, totalReadingTime: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) {
                it.copy(
                    lastReadTime = LocalDateTime.now(),
                    totalReadTime = it.totalReadTime + totalReadingTime
                )
            }
        }
    }

    fun addToReadingBook(bookId: Int) {
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
}