package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.WorkInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val webBookDataSource: WebBookDataSource
) : ViewModel() {
    private val _uiState = MutableDetailUiState()
    private var cacheBookProgressCollectJob: Job? = null
    var navController: NavController? = null
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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isCached = bookRepository.getIsBookCached(bookId)
        }
        bookRepository.getCacheBookProgressFlow(bookId)?.let { flow ->
            cacheBookProgressCollectJob?.cancel()
            cacheBookProgressCollectJob =
                viewModelScope.launch(Dispatchers.IO) {
                flow.collect {
                    _uiState.cacheProgress = it
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.getBookshelfBookMetadataFlow(bookId).collect {
                _uiState.isInBookshelf = it != null
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun cacheBook(bookId: Int): Flow<WorkInfo?> {
        val work = bookRepository.cacheBook(bookId)
        val isCachedFlow = bookRepository.isCacheBookWorkFlow(work.id)
        viewModelScope.launch(Dispatchers.IO) {
            isCachedFlow.collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.RUNNING) {
                    bookRepository.getCacheBookProgressFlow(bookId)?.let { flow ->
                        cacheBookProgressCollectJob?.cancel()
                        cacheBookProgressCollectJob =
                            viewModelScope.launch(Dispatchers.IO) {
                                flow.collect {
                                    _uiState.cacheProgress = it
                                }
                            }
                    }
                }
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.cacheProgress = -1
                    _uiState.isCached = bookRepository.getIsBookCached(bookId)
                }
            }
        }
        return isCachedFlow
    }

    fun onClickTag(tag: String) {
        if (navController == null) return
        webBookDataSource.progressBookTagClick(tag, navController!!)
    }
}