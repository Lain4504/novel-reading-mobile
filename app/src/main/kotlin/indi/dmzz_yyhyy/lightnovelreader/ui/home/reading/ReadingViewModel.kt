package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataReadingViewModel: UserDataRepository
) : ViewModel() {
    private val _uiState = MutableReadingUiState()
    private val readingBooksUserData = userDataReadingViewModel.intListUserData(UserDataPath.ReadingBooks.path)
    val uiState: ReadingUiState = _uiState

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateReadingBooks()
        }
    }

    private suspend fun updateReadingBooks() {
        readingBooksUserData.getFlowWithDefault(emptyList()).collect { ids ->
            _uiState.recentReadingBookInformation = ids.mapNotNull {
                if (it == -1) return@mapNotNull null
                bookRepository.getBookInformation(it)
            }.toMutableList()
            _uiState.recentReadingUserReadingData = ids.mapNotNull {
                if (it == -1) return@mapNotNull null
                bookRepository.getUserReadingData(it)
            }.toMutableList()
        }
    }
}
