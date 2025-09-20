package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.explore.ExploreRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpandedPageViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookshelfRepository: BookshelfRepository,
    private val textProcessingRepository: TextProcessingRepository
) : ViewModel() {
    private var expandedPageDataSource: ExploreExpandedPageDataSource? = null
    private var explorationExpandedPageBookListCollectJob: Job? = null
    private var loadMoreJob: Job? = null
    private var lastExpandedPageDataSourceId: String = ""
    private val _uiState = MutableExpandedPageUiState()
    val uiState: ExpandedPageUiState = _uiState

    fun init(expandedPageDataSourceId: String) {
        if (expandedPageDataSourceId == lastExpandedPageDataSourceId) return
        lastExpandedPageDataSourceId = expandedPageDataSourceId
        loadMoreJob?.cancel()
        explorationExpandedPageBookListCollectJob?.cancel()
        expandedPageDataSource = exploreRepository.exploreExpandedPageDataSourceMap[expandedPageDataSourceId]
        explorationExpandedPageBookListCollectJob = viewModelScope.launch(Dispatchers.IO) {
            expandedPageDataSource?.let { explorationExpandedPageDataSource ->
                explorationExpandedPageDataSource.refresh()
                _uiState.pageTitle = textProcessingRepository.processText {
                    explorationExpandedPageDataSource.getTitle()
                }
                _uiState.filters.clear()
                _uiState.filters.addAll(explorationExpandedPageDataSource.getFilters())
                explorationExpandedPageDataSource.getResultFlow().collect { result ->
                    _uiState.bookList.clear()
                    _uiState.bookList.addAll(
                        result.map {
                            textProcessingRepository.processBookInformation { it }
                        }
                    )
                    if (result.isEmpty()) { explorationExpandedPageDataSource.loadMore() }
                }
            }
        }
        viewModelScope.launch {
            bookshelfRepository.getAllBookshelfBookIdsFlow().collect {
                _uiState.allBookshelfBookIds.clear()
                _uiState.allBookshelfBookIds.addAll(it)
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
