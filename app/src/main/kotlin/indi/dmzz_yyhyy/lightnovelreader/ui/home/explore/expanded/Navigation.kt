package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

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
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.exploreExpandDestination() {
    composable<Route.Main.Exploration.Expanded>(

    ) { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val explorationExpandedPageHomeViewModel = hiltViewModel<ExpandedPageViewModel>()
        var dialog : @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
        ExpandedPageScreen(
            exploreUiState = exploreViewModel.uiState,
            expandedPageUiState = explorationExpandedPageHomeViewModel.uiState,
            refresh = exploreViewModel::refresh,
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
    if (!this.isResumed()) return
    navigate(Route.Main.Exploration.Expanded(expandedPageDataSourceId))
}