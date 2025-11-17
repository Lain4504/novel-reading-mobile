package com.miraimagiclab.novelreadingapp.ui.home.bookshelf.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.bookshelf.BookshelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteBookshelfDialogViewModel @Inject constructor(
    private val bookshelfRepository: BookshelfRepository
) : ViewModel() {
    fun deleteBookshelf(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookshelfRepository.deleteBookshelf(id)
        }
    }
}