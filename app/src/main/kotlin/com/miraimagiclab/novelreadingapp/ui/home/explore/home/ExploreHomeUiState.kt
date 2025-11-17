package com.miraimagiclab.novelreadingapp.ui.home.explore.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import io.lain4504.novelreadingapp.api.explore.ExploreBooksRow

@State
interface ExploreHomeUiState {
    val pageTitles: List<String>
    val selectedPage: Int
    val explorePageTitle: String
    val explorePageBooksRawList: List<ExploreBooksRow>
}

class MutableExploreHomeUiState : ExploreHomeUiState {
    override var pageTitles: List<String> by mutableStateOf(mutableListOf())
    override var selectedPage by mutableIntStateOf(0)
    override var explorePageTitle by mutableStateOf("")
    override var explorePageBooksRawList: List<ExploreBooksRow> by mutableStateOf(mutableListOf())
}