package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import kotlinx.coroutines.flow.Flow

@Stable
interface ReadingUiState {
    val recentReadingBookInformation: List<Flow<BookInformation>>
    val recentReadingUserReadingData: List<Flow<UserReadingData>>
}

class MutableReadingUiState: ReadingUiState {
    override var recentReadingBookInformation: MutableList<Flow<BookInformation>> = mutableStateListOf()
    override var recentReadingUserReadingData: MutableList<Flow<UserReadingData>> = mutableStateListOf()
}