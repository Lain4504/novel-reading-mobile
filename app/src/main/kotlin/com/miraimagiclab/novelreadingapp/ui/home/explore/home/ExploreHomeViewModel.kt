package com.miraimagiclab.novelreadingapp.ui.home.explore.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.explore.ExploreRepository
import com.miraimagiclab.novelreadingapp.data.text.TextProcessingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    private val exploreRepository: ExploreRepository
) : ViewModel() {
    private val _uiState = MutableExploreHomeUiState()
    private var workingExplorePageJob: Job? = null
    private var workingExploreBooksRowsJob: Job? = null
    val uiState: ExploreHomeUiState = _uiState

    fun init() {
        changePage(_uiState.selectedPage)
    }

    fun changePage(page: Int) {
        workingExplorePageJob?.cancel()
        workingExploreBooksRowsJob?.cancel()
        
        val explorePageIdList = exploreRepository.explorePageIdList
        if (explorePageIdList.isEmpty()) {
            _uiState.pageTitles = emptyList()
            _uiState.explorePageBooksRawList = emptyList()
            _uiState.explorePageTitle = ""
            return
        }
        
        val validPage = page.coerceIn(0, explorePageIdList.size - 1)
        _uiState.selectedPage = validPage
        
        workingExplorePageJob = viewModelScope.launch {
            val selectedId = explorePageIdList[validPage]
            val explorePageMap = exploreRepository.explorePageDataSourceMap
            _uiState.pageTitles = explorePageMap.map { it.value.title }
            workingExploreBooksRowsJob = viewModelScope.launch {
                explorePageMap[selectedId]?.getExplorePage()?.let { explorePage ->
                    _uiState.explorePageTitle = textProcessingRepository.processText { explorePage.title }
                    explorePage.rows.collect { exploreBooksRows ->
                        _uiState.explorePageBooksRawList =
                            exploreBooksRows.map { exploreBooksRow ->
                                exploreBooksRow.copy(
                                    bookList = exploreBooksRow.bookList.map {
                                        textProcessingRepository.processExploreBooksRow(it)
                                    }
                                )
                            }
                        }
                }
            }
        }
    }

    fun refresh() {
        _uiState.explorePageBooksRawList = emptyList()
        changePage(_uiState.selectedPage)
    }
}
