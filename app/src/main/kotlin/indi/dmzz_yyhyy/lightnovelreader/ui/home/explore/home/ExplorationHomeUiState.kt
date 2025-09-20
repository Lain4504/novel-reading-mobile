package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow

@State
interface ExplorationHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val explorationPageTitle: String
    val explorationPageBooksRawList: List<ExploreBooksRow>
}

class MutableExplorationHomeUiState : ExplorationHomeUiState {
    override var pageTitles: List<String> by mutableStateOf(mutableListOf())
    override var selectedPage by mutableIntStateOf(0)
    override var explorationPageTitle by mutableStateOf("")
    override var explorationPageBooksRawList: List<ExploreBooksRow> by mutableStateOf(mutableListOf())
}