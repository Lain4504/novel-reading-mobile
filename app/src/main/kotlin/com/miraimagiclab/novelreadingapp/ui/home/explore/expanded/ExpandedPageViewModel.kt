package com.miraimagiclab.novelreadingapp.ui.home.explore.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.bookshelf.BookshelfRepository
import com.miraimagiclab.novelreadingapp.data.explore.ExploreRepository
import com.miraimagiclab.novelreadingapp.data.text.TextProcessingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.web.explore.ExploreExpandedPageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExpandedPageViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository
) : ViewModel() {
    private var expandedPageDataSource: ExploreExpandedPageDataSource? = null
    private var exploreExpandedPageBookListCollectJob: Job? = null
    private var loadMoreJob: Job? = null
    private var lastExpandedPageDataSourceId: String = ""
    private val _uiState = MutableExpandedPageUiState()
    val uiState: ExpandedPageUiState = _uiState

    fun init(expandedPageDataSourceId: String) {
        if (expandedPageDataSourceId == lastExpandedPageDataSourceId) return
        lastExpandedPageDataSourceId = expandedPageDataSourceId
        loadMoreJob?.cancel()
        exploreExpandedPageBookListCollectJob?.cancel()

        expandedPageDataSource = exploreRepository.exploreExpandedPageDataSourceMap[expandedPageDataSourceId]

        exploreExpandedPageBookListCollectJob = viewModelScope.launch {
            expandedPageDataSource?.let { dataSource ->
                withContext(Dispatchers.IO) { dataSource.refresh() }
                val processedTitle = withContext(Dispatchers.IO) {
                    textProcessingRepository.processText { dataSource.getTitle() }
                }
                val filters = withContext(Dispatchers.IO) { dataSource.getFilters() }
                _uiState.pageTitle = processedTitle
                _uiState.filters.clear()
                _uiState.filters.addAll(filters)

                dataSource.getResultFlow().collect { rawResult ->
                    val processedList = withContext(Dispatchers.IO) {
                        rawResult.map { book ->
                            textProcessingRepository.processBookInformation { book }
                        }
                    }
                    _uiState.bookList = processedList

                    if (processedList.isEmpty()) {
                        withContext(Dispatchers.IO) {
                            dataSource.loadMore()
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect { ids ->
                _uiState.allBookshelfBookIds = ids.toList()
            }
        }
    }

    fun loadMore() {
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch(Dispatchers.IO) {
            if (expandedPageDataSource?.hasMore() == false) return@launch
            expandedPageDataSource?.loadMore()
        }
    }

    fun clear() {
        lastExpandedPageDataSourceId = ""
    }

    fun refresh() {
        init(lastExpandedPageDataSourceId)
    }
}
