package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.explorationExpandDestination(navController: NavController) {
    composable<Route.Home.Exploration.Expanded> { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Home) }
        val explorationViewModel = hiltViewModel<ExplorationViewModel>(parentEntry)
        val explorationExpandedPageHomeViewModel = hiltViewModel<ExpandedPageViewModel>()
        ExpandedPageScreen(
            explorationUiState = explorationViewModel.uiState,
            explorationExpandedPageUiState = explorationExpandedPageHomeViewModel.uiState,
            refresh = explorationViewModel::refresh,
            dialog = {
                //FIXME
            },
            expandedPageDataSourceId = entry.toRoute<Route.Home.Exploration.Expanded>().expandedPageDataSourceId,
            init = explorationExpandedPageHomeViewModel::init,
            loadMore = explorationExpandedPageHomeViewModel::loadMore,
            requestAddBookToBookshelf = {
                //FIXME
            },
            onClickBack = {
                explorationExpandedPageHomeViewModel.clear()
                navController.popBackStack()
            },
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            }
        )
    }
}

fun NavController.navigateToExplorationExpandDestination(expandedPageDataSourceId: String) {
    navigate(Route.Home.Exploration.Expanded(expandedPageDataSourceId))
}