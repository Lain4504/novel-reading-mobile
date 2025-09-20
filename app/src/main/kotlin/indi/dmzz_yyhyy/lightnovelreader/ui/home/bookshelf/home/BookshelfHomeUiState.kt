package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.Bookshelf
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.MutableBookshelf

@State
interface BookshelfHomeUiState {
    val bookshelfList: List<Bookshelf>
    val selectedBookshelfId: Int
    val bookInformationMap: Map<Int, BookInformation>
    val bookLastChapterTitleMap: Map<Int, String>
    val selectedTabIndex get() = bookshelfList.indexOfFirst { it.id == selectedBookshelfId }
    val selectedBookshelf: Bookshelf get() = if (selectedTabIndex != -1) bookshelfList[selectedTabIndex] else MutableBookshelf()
    val selectMode: Boolean
    var updatedExpanded: Boolean
    var pinnedExpanded: Boolean
    var allExpanded: Boolean
    val selectedBookIds: List<Int>
    val toast: String
}

class MutableBookshelfHomeUiState : BookshelfHomeUiState {
    override var bookshelfList by mutableStateOf(emptyList<MutableBookshelf>())
    override var selectedBookshelfId by mutableIntStateOf(-1)
    override var bookInformationMap = mutableStateMapOf<Int, BookInformation>()
    override var bookLastChapterTitleMap = mutableStateMapOf<Int, String>()
    override var selectMode by mutableStateOf(false)
    override var updatedExpanded by mutableStateOf(true)
    override var pinnedExpanded by mutableStateOf(true)
    override var allExpanded by mutableStateOf(true)
    override val selectedBookIds: MutableList<Int> = mutableStateListOf()
    override var toast by mutableStateOf("")
}