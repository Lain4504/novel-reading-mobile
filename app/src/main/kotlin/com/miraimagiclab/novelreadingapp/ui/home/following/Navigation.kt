package com.miraimagiclab.novelreadingapp.ui.home.following

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import io.lain4504.novelreadingapp.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.followingDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Following> {
        val navController = LocalNavController.current
        val followingViewModel = hiltViewModel<FollowingViewModel>()
        FollowingScreen(
            controller = navController,
            selectedRoute = Route.Main.Following,
            init = { followingViewModel.load(0) },
            onClickBook = navController::navigateToBookDetailDestination,
            uiState = followingViewModel.uiState,
            getBookInfoFlow = followingViewModel::getBookInfoStateFlow,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}

@Suppress("unused")
fun NavController.navigateToFollowingDestination() {
    navigate(Route.Main.Following)
}

