package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationRepository
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExplorationHomeViewModel @Inject constructor(
    private val textProcessingRepository: TextProcessingRepository,
    private val explorationRepository: ExplorationRepository
) : ViewModel() {
    private val _uiState = MutableExplorationHomeUiState()
    private var workingExplorationPageJob: Job? = null
    private var workingExplorationBooksRowsJob: Job? = null
    val uiState: ExplorationHomeUiState = _uiState

    fun init() {
        changePage(_uiState.selectedPage)
    }

    fun changePage(page: Int) {
        workingExplorationPageJob?.cancel()
        workingExplorationBooksRowsJob?.cancel()
        _uiState.selectedPage = page
        workingExplorationPageJob = viewModelScope.launch {
            val selectedId = explorationRepository.explorationPageIdList[page]
            val explorationPageMap = explorationRepository.explorationPageDataSourceMap
            _uiState.pageTitles = explorationPageMap.map { it.value.title }
            workingExplorationBooksRowsJob = viewModelScope.launch {
                explorationPageMap[selectedId]?.getExplorationPage()?.let { explorationPage ->
                    _uiState.explorationPageTitle = textProcessingRepository.processText { explorationPage.title }
                    explorationPage.rows.collect { explorationBooksRows ->
                        _uiState.explorationPageBooksRawList =
                            explorationBooksRows.map { explorationBooksRow ->
                                explorationBooksRow.copy(
                                    bookList = explorationBooksRow.bookList.map {
                                        textProcessingRepository.processExplorationBooksRow(it)
                                    }
                                )
                            }
                        }
                }
            }
        }
    }

    fun refresh() {
        _uiState.explorationPageBooksRawList = emptyList()
        changePage(_uiState.selectedPage)
    }
}
