package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.explorationSearchDestination(navController: NavController) {
    composable<Route.Main.Exploration.Search> { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val explorationViewModel = hiltViewModel<ExplorationViewModel>(parentEntry)
        val explorationSearchViewModel = hiltViewModel<ExplorationSearchViewModel>()
        ExplorationSearchScreen(
            explorationUiState = explorationViewModel.uiState,
            explorationSearchUiState = explorationSearchViewModel.uiState,
            refresh = explorationViewModel::refresh,
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
    navigate(Route.Main.Exploration.Search)
}