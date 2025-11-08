package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingHomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val readingBooksUserData = userDataRepository.stringListUserData(UserDataPath.ReadingBooks.path)
    var recentReadingBookIds: List<String> by mutableStateOf(listOf())
        private set
    private val _recentReadingBookInformationMap = mutableStateMapOf<String, BookInformation>()
    private val _recentReadingUserReadingDataMap = mutableStateMapOf<String, UserReadingData>()
    val recentReadingBookInformationMap: Map<String, BookInformation> = _recentReadingBookInformationMap
    val recentReadingUserReadingDataMap: Map<String, UserReadingData> = _recentReadingUserReadingDataMap

    init {
        viewModelScope.launch {
            readingBooksUserData.getFlowWithDefault(emptyList()).collect {
                recentReadingBookIds = it
                    .reversed()
                    .filter(String::isNotBlank)
            }
        }
    }

    fun loadBookInfo(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val info = bookRepository.getStateBookInformation(id, viewModelScope)
            val userData = bookRepository.getStateUserReadingData(id, viewModelScope)
            viewModelScope.launch(Dispatchers.Main) {
                _recentReadingBookInformationMap[id] = info
                _recentReadingUserReadingDataMap[id] = userData
            }
        }
    }


    fun removeFromReadingList(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBooksUserData.update {
                it.toMutableList().apply { remove(bookId) }
            }
        }
    }

    fun addToReadingList(bookId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            readingBooksUserData.update {
                it + listOf(bookId)
            }
        }
    }
}
