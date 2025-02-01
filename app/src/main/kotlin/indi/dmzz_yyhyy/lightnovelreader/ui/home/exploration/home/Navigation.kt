package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded.navigateToExplorationExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search.navigateToSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.explorationHomeDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Exploration.Home> { entry ->
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Home) }
        val explorationViewModel = hiltViewModel<ExplorationViewModel>(parentEntry)
        val explorationHomeViewModel = hiltViewModel<ExplorationHomeViewModel>()
        ExplorationHomeScreen(
            explorationUiState = explorationViewModel.uiState,
            explorationHomeUiState = explorationHomeViewModel.uiState,
            onClickExpand = navController::navigateToExplorationExpandDestination,
            onClickBook = navController::navigateToBookDetailDestination,
            init = explorationHomeViewModel::init,
            changePage = explorationHomeViewModel::changePage,
            onClickSearch = navController::navigateToSearchDestination,
            refresh = explorationHomeViewModel::refresh,
            selectedRoute = Route.Home.Exploration,
            controller = navController,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

fun NavController.navigateToExplorationHomeDestination() {
    navigate(Route.Home.Exploration.Home)
}