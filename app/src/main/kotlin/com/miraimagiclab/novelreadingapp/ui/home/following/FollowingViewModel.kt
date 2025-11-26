package com.miraimagiclab.novelreadingapp.ui.home.following

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.auth.TokenManager
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.userinteraction.FollowedNovelData
import com.miraimagiclab.novelreadingapp.data.userinteraction.UserNovelInteractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.book.MutableBookInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FollowingViewModel @Inject constructor(
    private val userNovelInteractionRepository: UserNovelInteractionRepository,
    private val bookRepository: BookRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableFollowingUiState()
    val uiState: FollowingUiState = _uiState

    private val bookInfoStateFlows = mutableMapOf<String, StateFlow<BookInformation>>()

    fun load(page: Int = 0) {
        if (!tokenManager.isUserAuthenticated()) {
            _uiState.error = "Vui lòng đăng nhập để xem danh sách theo dõi"
            _uiState.followedNovels = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isLoading = true
            _uiState.error = null

            userNovelInteractionRepository.getFollowedNovelsPaginated(page = page, size = 20).fold(
                onSuccess = { result ->
                    _uiState.currentPage = page
                    _uiState.totalPages = result.totalPages
                    _uiState.hasNext = result.hasNext
                    _uiState.hasPrevious = result.hasPrevious
                    
                    // Convert FollowedNovelData to BookInformation
                    _uiState.followedNovels = result.content.map { novelData ->
                        convertToBookInformation(novelData)
                    }
                    _uiState.isLoading = false
                },
                onFailure = { e ->
                    Log.e("FollowingViewModel", "Failed to load followed novels", e)
                    _uiState.error = "Không thể tải danh sách theo dõi: ${e.message}"
                    _uiState.isLoading = false
                }
            )
        }
    }

    fun loadNextPage() {
        if (_uiState.hasNext && !_uiState.isLoading) {
            load(_uiState.currentPage + 1)
        }
    }

    fun loadPreviousPage() {
        if (_uiState.hasPrevious && !_uiState.isLoading) {
            load(_uiState.currentPage - 1)
        }
    }

    fun refresh() {
        load(0)
    }

    fun getBookInfoStateFlow(id: String): StateFlow<BookInformation> {
        return bookInfoStateFlows.getOrPut(id) {
            bookRepository.getBookInformationFlow(id, viewModelScope)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = BookInformation.empty(id)
                )
        }
    }

    private fun convertToBookInformation(novelData: FollowedNovelData): BookInformation {
        val bookInfo = MutableBookInformation.empty()
        bookInfo.id = novelData.id
        bookInfo.title = novelData.title
        bookInfo.subtitle = ""
        bookInfo.author = novelData.authorName ?: ""
        bookInfo.description = novelData.description ?: ""
        bookInfo.coverUri = novelData.coverImage?.let { Uri.parse(it) } ?: Uri.EMPTY
        bookInfo.tags.clear()
        bookInfo.tags.addAll(novelData.tags.filterNotNull())
        bookInfo.wordCount = io.lain4504.novelreadingapp.api.book.WorldCount(novelData.wordCount)
        bookInfo.isComplete = novelData.status == "COMPLETED"
        
        // Parse dates
        val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
        try {
            novelData.updatedAt?.let { bookInfo.lastUpdated = LocalDateTime.parse(it, dateFormatter) }
        } catch (e: Exception) {
            // Use current date if parsing fails
            bookInfo.lastUpdated = LocalDateTime.now()
        }
        
        return bookInfo
    }
}

