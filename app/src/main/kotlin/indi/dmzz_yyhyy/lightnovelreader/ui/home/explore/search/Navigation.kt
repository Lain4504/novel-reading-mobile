package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.exploreSearchDestination() {
    composable<Route.Main.Exploration.Search> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val explorationSearchViewModel = hiltViewModel<ExplorationSearchViewModel>()
        ExplorationSearchScreen(
            exploreUiState = exploreViewModel.uiState,
            explorationSearchUiState = explorationSearchViewModel.uiState,
            refresh = exploreViewModel::refresh,
            requestAddBookToBookshelf = {
                navController.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = { navController.popBackStackIfResumed() },
            init = explorationSearchViewModel::init,
            onChangeSearchType = { explorationSearchViewModel.changeSearchType(it) },
            onSearch = { explorationSearchViewModel.search(it) },
            onClickDeleteHistory = { explorationSearchViewModel.deleteHistory(it) },
            onClickClearAllHistory = explorationSearchViewModel::clearAllHistory,
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            }
        )
    }
}

fun NavController.navigateToSearchDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Exploration.Search)
}