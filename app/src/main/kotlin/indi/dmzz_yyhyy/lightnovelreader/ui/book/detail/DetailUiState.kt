package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem

@State
interface DetailUiState {
    val bookInformation: BookInformation
    val bookVolumes: BookVolumes
    val userReadingData: UserReadingData
    val isCached: Boolean
    val downloadItem: DownloadItem?
    val isInBookshelf: Boolean
}

class MutableDetailUiState: DetailUiState {
    override var bookInformation: BookInformation by mutableStateOf(BookInformation.empty())
    override var bookVolumes: BookVolumes by mutableStateOf(BookVolumes.empty(-1))
    override var userReadingData: UserReadingData by mutableStateOf(UserReadingData.empty())
    override var isCached: Boolean by mutableStateOf(false)
    override var downloadItem: DownloadItem? by mutableStateOf(null)
    override var isInBookshelf: Boolean by mutableStateOf(false)
}


