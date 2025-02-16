package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookshelfRepository: BookshelfRepository
) : ViewModel() {
    private val _uiState = MutableDetailUiState()
    val uiState: DetailUiState = _uiState

    fun init(bookId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookInformation(bookId).collect {
                if (it.id == -1) return@collect
                _uiState.bookInformation = it
                val bookshelfBookMetadata = bookshelfRepository.getBookshelfBookMetadata(bookId) ?: return@collect
                bookshelfBookMetadata.bookShelfIds.forEach { bookshelfId ->
                    bookshelfRepository.deleteBookFromBookshelfUpdatedBookIds(bookshelfId, bookId)
                }
                bookshelfRepository.updateBookshelfBookMetadataLastUpdateTime(bookId, it.lastUpdated)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumes(bookId).collect {
                if (it.volumes.isEmpty()) return@collect
                _uiState.bookVolumes = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingData(bookId).collect {
                _uiState.userReadingData = it
            }
        }
    }

    fun cacheBook(bookId: Int): Flow<WorkInfo?> {
        val work = bookRepository.cacheBook(bookId)
        return bookRepository.isCacheBookWorkFlow(work.id)
    }
}