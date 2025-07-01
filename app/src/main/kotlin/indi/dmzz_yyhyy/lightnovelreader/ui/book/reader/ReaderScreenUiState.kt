package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentUiState

@Stable
interface ReaderScreenUiState {
    val bookId: Int
    val userReadingData: UserReadingData
    val bookVolumes: BookVolumes
    val contentUiState: ContentUiState
}

class MutableReaderScreenUiState(
    contentUiState: ContentUiState
): ReaderScreenUiState {
    override var bookId by mutableIntStateOf(-1)
    override var userReadingData by mutableStateOf(UserReadingData.empty())
    override var bookVolumes by mutableStateOf(BookVolumes.empty())
    override var contentUiState by mutableStateOf(contentUiState)
}