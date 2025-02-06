package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataReadingViewModel: UserDataRepository
) : ViewModel() {
    private val readingBooksUserData = userDataReadingViewModel.intListUserData(UserDataPath.ReadingBooks.path)
    val _recentReadingBookInformation: SnapshotStateList<Flow<BookInformation>> = mutableStateListOf()
    val _recentReadingUserReadingData: SnapshotStateList<Flow<UserReadingData>> = mutableStateListOf()
    val recentReadingBookInformation: List<Flow<BookInformation>> = _recentReadingBookInformation
    val recentReadingUserReadingData: List<Flow<UserReadingData>> = _recentReadingUserReadingData

    init {
        viewModelScope.launch(Dispatchers.IO) {
            updateReadingBooks()
        }
    }

    private suspend fun updateReadingBooks() {
        readingBooksUserData.getFlowWithDefault(emptyList()).collect { ids ->
            ids.mapNotNull {
                if (it == -1) return@mapNotNull null
                bookRepository.getBookInformation(it)
            }.let(_recentReadingBookInformation::addAll)
            ids.mapNotNull {
                if (it == -1) return@mapNotNull null
                bookRepository.getUserReadingData(it)
            }.let(_recentReadingUserReadingData::addAll)
        }
    }
}
