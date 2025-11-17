package com.miraimagiclab.novelreadingapp.ui.dialog

import androidx.compose.runtime.mutableStateListOf
import io.lain4504.novelreadingapp.api.bookshelf.Bookshelf

interface AddToBookshelfDialogUiState {
    val allBookShelf: List<Bookshelf>
    val selectedBookshelfIds: List<Int>
}

class MutableAddToBookshelfDialogUiState: AddToBookshelfDialogUiState {
    override var allBookShelf = mutableStateListOf<Bookshelf>()
    override var selectedBookshelfIds  = mutableStateListOf<Int>()
}