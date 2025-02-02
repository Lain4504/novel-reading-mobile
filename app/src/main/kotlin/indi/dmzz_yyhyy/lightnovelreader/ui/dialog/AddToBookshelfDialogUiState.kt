package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.mutableStateListOf
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.Bookshelf

interface AddToBookshelfDialogUiState {
    val allBookShelf: List<Bookshelf>
    val selectedBookshelfIds: List<Int>
}

class MutableAddToBookshelfDialogUiState: AddToBookshelfDialogUiState {
    override var allBookShelf = mutableStateListOf<Bookshelf>()
    override var selectedBookshelfIds  = mutableStateListOf<Int>()
}