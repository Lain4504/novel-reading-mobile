package com.miraimagiclab.novelreadingapp.ui.home.explore.search
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.bookshelf.BookshelfRepository
import com.miraimagiclab.novelreadingapp.data.explore.ExploreRepository
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreSearchViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableExploreSearchUiState()
    private val searchHistoryUserData = userDataRepository.stringListUserData(UserDataPath.Search.History.path)
    private var searchTypeTipMap = emptyMap<String, String>()
    private var searchJob: Job? = null
    val uiState: ExploreSearchUiState = _uiState

    override fun onCleared() {
        super.onCleared()
        exploreRepository.stopAllSearch()
        searchJob?.cancel()
    }

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            searchTypeTipMap = exploreRepository.searchTipMap
            _uiState.searchTypeNameMap = exploreRepository.searchTypeMap
            _uiState.searchTypeIdList = exploreRepository.searchTypeIdList.toMutableList()
            _uiState.searchType = exploreRepository.searchTypeIdList.getOrNull(0) ?: return@launch
            _uiState.searchTip = searchTypeTipMap.getOrDefault(_uiState.searchType, "")
            searchHistoryUserData.getFlow().collect {
                it?.let {
                    _uiState.historyList = it.reversed().toMutableList()
                }
            }
        }
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect {
                _uiState.allBookshelfBookIds = it.toMutableList()
            }
        }
    }

    fun changeSearchType(searchTypeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.searchType = searchTypeId
            _uiState.searchTip = searchTypeTipMap.getOrDefault(_uiState.searchType, "")
        }
    }

    fun updateSearchKeyword(keyword: String) {
        _uiState.currentKeyword = keyword
    }

    fun deleteHistory(history: String) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update {
                val newList = it.toMutableList()
                newList.remove(history)
                return@update newList
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update { emptyList() }
        }
    }

    fun search(keyword: String) {
        val trimmedKeyword = keyword.trim()
        if (trimmedKeyword.isEmpty()) {
            exploreRepository.stopAllSearch()
            searchJob?.cancel()
            _uiState.currentKeyword = ""
            _uiState.isLoading = false
            _uiState.isLoadingComplete = true
            _uiState.searchResult = emptyList()
            return
        }
        _uiState.currentKeyword = trimmedKeyword
        exploreRepository.stopAllSearch()
        _uiState.isLoading = true
        _uiState.isLoadingComplete = false
        _uiState.searchResult = mutableListOf()
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            exploreRepository.search(_uiState.searchType, trimmedKeyword).collect {
                _uiState.isLoading = false
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    _uiState.isLoadingComplete = true
                    val finalResults = it.dropLast(1)
                    if (finalResults.isNotEmpty()) {
                        _uiState.searchResult = finalResults.toMutableList()
                    }
                    return@collect
                }
                _uiState.searchResult = it.toMutableList()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryUserData.update {
                val newList = it.toMutableList()
                if (it.contains(trimmedKeyword))
                    newList.remove(trimmedKeyword)
                newList.add(trimmedKeyword)
                return@update newList
            }
        }
    }
}
