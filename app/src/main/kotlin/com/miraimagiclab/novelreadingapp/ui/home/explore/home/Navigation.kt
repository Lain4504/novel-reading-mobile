package com.miraimagiclab.novelreadingapp.ui.home.explore.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.home.explore.ExploreViewModel
import com.miraimagiclab.novelreadingapp.ui.home.explore.expanded.navigateToExploreExpandDestination
import com.miraimagiclab.novelreadingapp.ui.home.explore.search.navigateToSearchDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.exploreHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Explore.Home> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val exploreHomeViewModel = hiltViewModel<ExploreHomeViewModel>()
        ExploreHomeScreen(
            exploreUiState = exploreViewModel.uiState,
            exploreHomeUiState = exploreHomeViewModel.uiState,
            onClickExpand = navController::navigateToExploreExpandDestination,
            onClickBook = navController::navigateToBookDetailDestination,
            init = exploreHomeViewModel::init,
            changePage = exploreHomeViewModel::changePage,
            onClickSearch = navController::navigateToSearchDestination,
            refresh = exploreHomeViewModel::refresh,
            selectedRoute = Route.Main.Explore,
            controller = navController,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

fun NavController.navigateToExploreHomeDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Home)
}