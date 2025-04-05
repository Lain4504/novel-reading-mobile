package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.explorationExpandDestination() {
    composable<Route.Main.Exploration.Expanded>(
        enterTransition = { expandEnter() },
        exitTransition = { expandExit() },
        popEnterTransition = { expandPopEnter() },
        popExitTransition = { expandPopExit() }
    ) { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val explorationViewModel = hiltViewModel<ExplorationViewModel>(parentEntry)
        val explorationExpandedPageHomeViewModel = hiltViewModel<ExpandedPageViewModel>()
        var dialog : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
        ExpandedPageScreen(
            explorationUiState = explorationViewModel.uiState,
            explorationExpandedPageUiState = explorationExpandedPageHomeViewModel.uiState,
            refresh = explorationViewModel::refresh,
            dialog = { newDialog -> dialog = newDialog },
            expandedPageDataSourceId = entry.toRoute<Route.Main.Exploration.Expanded>().expandedPageDataSourceId,
            init = explorationExpandedPageHomeViewModel::init,
            loadMore = explorationExpandedPageHomeViewModel::loadMore,
            requestAddBookToBookshelf = {
                navController.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = {
                explorationExpandedPageHomeViewModel.clear()
                navController.popBackStackIfResumed()
            },
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            }
        )
        dialog.invoke()
    }
}

fun NavController.navigateToExplorationExpandDestination(expandedPageDataSourceId: String) {
    navigate(Route.Main.Exploration.Expanded(expandedPageDataSourceId))
}