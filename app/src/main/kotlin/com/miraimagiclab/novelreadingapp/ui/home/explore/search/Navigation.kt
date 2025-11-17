package com.miraimagiclab.novelreadingapp.ui.home.explore.search

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.dialog.navigateToAddBookToBookshelfDialog
import com.miraimagiclab.novelreadingapp.ui.home.explore.ExploreViewModel
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.exploreSearchDestination() {
    composable<Route.Main.Explore.Search> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val exploreSearchViewModel = hiltViewModel<ExploreSearchViewModel>()
        ExploreSearchScreen(
            exploreUiState = exploreViewModel.uiState,
            exploreSearchUiState = exploreSearchViewModel.uiState,
            refresh = exploreViewModel::refresh,
            requestAddBookToBookshelf = {
                navController.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = { navController.popBackStackIfResumed() },
            init = exploreSearchViewModel::init,
            onChangeSearchType = { exploreSearchViewModel.changeSearchType(it) },
            onSearch = { exploreSearchViewModel.search(it) },
            onClickDeleteHistory = { exploreSearchViewModel.deleteHistory(it) },
            onClickClearAllHistory = exploreSearchViewModel::clearAllHistory,
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            }
        )
    }
}

fun NavController.navigateToSearchDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Search)
}