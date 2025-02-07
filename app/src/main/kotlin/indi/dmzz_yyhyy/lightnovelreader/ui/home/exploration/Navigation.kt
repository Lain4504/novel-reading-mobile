package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded.explorationExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.home.explorationHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search.explorationSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.explorationNavigation(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Home.Exploration>(
        startDestination = Route.Home.Exploration.Home,
    ) {
        explorationHomeDestination(navController, sharedTransitionScope)
        explorationExpandDestination(navController)
        explorationSearchDestination(navController)
    }
}

@Suppress("unused")
fun NavController.navigateToExplorationNavigation() {
    navigate(Route.Home.Exploration)
}