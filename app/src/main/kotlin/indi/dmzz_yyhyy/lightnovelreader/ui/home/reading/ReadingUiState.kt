package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Stable
interface ReadingUiState {
    val recentReadingBookInformation: List<Flow<BookInformation>>
    val recentReadingUserReadingData: List<Flow<UserReadingData>>
}

class MutableReadingUiState: ReadingUiState {
    override var recentReadingBookInformation: MutableList<Flow<BookInformation>> = mutableStateListOf()
    override var recentReadingUserReadingData: MutableList<Flow<UserReadingData>> = mutableStateListOf()
}