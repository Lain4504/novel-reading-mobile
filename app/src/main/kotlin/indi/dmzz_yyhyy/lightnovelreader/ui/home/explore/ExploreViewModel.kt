package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val webBookDataSource: WebBookDataSource
) : ViewModel() {
    private var _uiState = MutableExploreUiState()
    val uiState: ExploreUiState = _uiState

    init {
        _uiState.isOffLine = webBookDataSource.offLine
        viewModelScope.launch(Dispatchers.IO) {
            webBookDataSource.isOffLineFlow.collect {
                _uiState.isOffLine = it
            }
        }
    }

    fun refresh() {
        _uiState.isRefreshing = true
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.isOffLine = webBookDataSource.isOffLine()
            _uiState.isRefreshing = false
        }
    }
}