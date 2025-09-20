package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExplorationExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search.navigateToSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.exploreHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Exploration.Home> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val explorationHomeViewModel = hiltViewModel<ExplorationHomeViewModel>()
        ExplorationHomeScreen(
            exploreUiState = exploreViewModel.uiState,
            explorationHomeUiState = explorationHomeViewModel.uiState,
            onClickExpand = navController::navigateToExplorationExpandDestination,
            onClickBook = navController::navigateToBookDetailDestination,
            init = explorationHomeViewModel::init,
            changePage = explorationHomeViewModel::changePage,
            onClickSearch = navController::navigateToSearchDestination,
            refresh = explorationHomeViewModel::refresh,
            selectedRoute = Route.Main.Exploration,
            controller = navController,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

fun NavController.navigateToExplorationHomeDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Exploration.Home)
}