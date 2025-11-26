package com.miraimagiclab.novelreadingapp.ui.book.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import android.widget.Toast
import com.miraimagiclab.novelreadingapp.data.auth.TokenManager
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.download.DownloadProgressRepository
import com.miraimagiclab.novelreadingapp.data.download.DownloadType
import com.miraimagiclab.novelreadingapp.data.userinteraction.UserNovelInteractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val downloadProgressRepository: DownloadProgressRepository,
    private val workManager: WorkManager,
    private val userNovelInteractionRepository: UserNovelInteractionRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {
    private val _uiState = MutableDetailUiState()
    var navController: NavController? = null
    val uiState: DetailUiState = _uiState

    var isInitialized by mutableStateOf(false)
        private set

    fun init(bookId: String) {
        Log.d("DetailViewModel", "Init bookId = $bookId")
        if (isInitialized) return
        isInitialized = true
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookInformationFlow(bookId, viewModelScope).collect {
                if (it.id.isBlank()) return@collect
                _uiState.bookInformation = it
                _uiState.isLoading = false
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId, viewModelScope).collect {
                if (it.volumes.isEmpty()) return@collect
                _uiState.bookVolumes = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingDataFlow(bookId).collect {
                _uiState.userReadingData = it
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isCached = bookRepository.getIsBookCached(bookId)
        }
        // Query user novel interaction for follow status
        viewModelScope.launch(Dispatchers.IO) {
            if (tokenManager.isUserAuthenticated()) {
                userNovelInteractionRepository.getUserNovelInteraction(bookId).fold(
                    onSuccess = { interaction ->
                        _uiState.hasFollowing = interaction.hasFollowing
                    },
                    onFailure = { e ->
                        Log.e("DetailViewModel", "Failed to get user novel interaction", e)
                        _uiState.hasFollowing = false
                    }
                )
            } else {
                _uiState.hasFollowing = false
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            downloadProgressRepository.downloadItemIdListFlow.collect { downloadItemList ->
                _uiState.downloadItem = downloadItemList.findLast { it.bookId == _uiState.bookInformation.id && it.type == DownloadType.CACHE }
            }
        }
    }
    
    fun followNovel(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!tokenManager.isUserAuthenticated()) {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Vui lòng đăng nhập để theo dõi truyện", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            
            userNovelInteractionRepository.followNovel(bookId).fold(
                onSuccess = { result ->
                    _uiState.hasFollowing = result.hasFollowing
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { e ->
                    Log.e("DetailViewModel", "Failed to follow novel", e)
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Không thể theo dõi truyện: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
    
    fun unfollowNovel(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!tokenManager.isUserAuthenticated()) {
                return@launch
            }
            
            userNovelInteractionRepository.unfollowNovel(bookId).fold(
                onSuccess = { result ->
                    _uiState.hasFollowing = false
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = { e ->
                    Log.e("DetailViewModel", "Failed to unfollow novel", e)
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Không thể bỏ theo dõi truyện: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    fun cacheBook(bookId: String): Flow<WorkInfo?> {
        val work = bookRepository.cacheBook(bookId)
        val isCachedFlow = bookRepository.isCacheBookWorkFlow(work.id)
        viewModelScope.launch(Dispatchers.IO) {
            isCachedFlow.collect { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    _uiState.isCached = bookRepository.getIsBookCached(bookId)
                }
            }
        }
        return isCachedFlow
    }

    fun onClickTag(tag: String) {
        if (navController == null) return
        bookRepository.progressBookTagClick(tag, navController!!)
    }
}