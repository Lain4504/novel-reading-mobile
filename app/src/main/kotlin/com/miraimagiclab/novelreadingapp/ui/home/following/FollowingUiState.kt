package com.miraimagiclab.novelreadingapp.ui.home.following

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.lain4504.novelreadingapp.api.book.BookInformation

@State
interface FollowingUiState {
    val followedNovels: List<BookInformation>
    val isLoading: Boolean
    val currentPage: Int
    val totalPages: Int
    val hasNext: Boolean
    val hasPrevious: Boolean
    val error: String?
}

class MutableFollowingUiState : FollowingUiState {
    override var followedNovels by mutableStateOf<List<BookInformation>>(emptyList())
    override var isLoading by mutableStateOf(false)
    override var currentPage by mutableStateOf(0)
    override var totalPages by mutableStateOf(0)
    override var hasNext by mutableStateOf(false)
    override var hasPrevious by mutableStateOf(false)
    override var error by mutableStateOf<String?>(null)
}

