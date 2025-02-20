package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.events.ReadingEvent
import indi.dmzz_yyhyy.lightnovelreader.utils.events.ReadingEventHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToBookshelfDialogViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository,
    private val bookRepository: BookRepository,
    private val readingEventHandler: ReadingEventHandler

) : ViewModel() {
    private val _addToBookshelfDialogUiState = MutableAddToBookshelfDialogUiState()
    var navController: NavController? = null
    var bookId: Int = -1
        set(value) {
            viewModelScope.launch(Dispatchers.IO) {
                _addToBookshelfDialogUiState.allBookShelf.addAll(
                    bookshelfRepository.getAllBookshelfIds()
                        .mapNotNull { bookshelfRepository.getBookshelf(it) }
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                _addToBookshelfDialogUiState.selectedBookshelfIds.addAll(bookshelfRepository.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList())
            }
            field = value
        }
    val addToBookshelfDialogUiState = _addToBookshelfDialogUiState

    fun onSelectBookshelf(bookshelfId: Int) {
        if (bookId == -1) return
        _addToBookshelfDialogUiState.selectedBookshelfIds += listOf(bookshelfId)
    }

    fun onDeselectBookshelf(bookshelfId: Int) {
        if (bookId == -1) return
        _addToBookshelfDialogUiState.selectedBookshelfIds =
            _addToBookshelfDialogUiState.selectedBookshelfIds.apply { removeAll { it == bookshelfId } }
    }

    fun onDismissAddToBookshelfRequest() {
        navController?.popBackStack()
        if (bookId == -1) return
        _addToBookshelfDialogUiState.selectedBookshelfIds.clear()
    }

    fun processAddToBookshelfRequest() {
        navController?.popBackStack()
        if (bookId == -1) return
        viewModelScope.launch(Dispatchers.IO) {
            val oldBookShelfIds = bookshelfRepository.getBookshelfBookMetadata(bookId)?.bookShelfIds ?: emptyList()
            viewModelScope.launch(Dispatchers.IO) {
                bookRepository.getBookInformation(bookId).collect { bookInformation ->
                    if (bookInformation.isEmpty()) return@collect
                    _addToBookshelfDialogUiState.selectedBookshelfIds.forEach {
                        println("FAV, id = $bookId, now sending book fav event")
                        readingEventHandler.sendEvent(ReadingEvent.BookFavorite(bookId))
                        bookshelfRepository.addBookIntoBookShelf(it, bookInformation)
                    }
                }
            }
            oldBookShelfIds.filter { !_addToBookshelfDialogUiState.selectedBookshelfIds.contains(it) }.forEach {
                bookshelfRepository.deleteBookFromBookshelf(it, bookId)
            }
        }
    }
}