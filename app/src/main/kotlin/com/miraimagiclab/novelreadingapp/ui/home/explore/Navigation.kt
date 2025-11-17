package com.miraimagiclab.novelreadingapp.ui.home.explore

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.home.explore.expanded.exploreExpandDestination
import com.miraimagiclab.novelreadingapp.ui.home.explore.home.exploreHomeDestination
import com.miraimagiclab.novelreadingapp.ui.home.explore.search.exploreSearchDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.exploreNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Explore>(
        startDestination = Route.Main.Explore.Home,
    ) {
        exploreHomeDestination(sharedTransitionScope)
        exploreExpandDestination()
        exploreSearchDestination()
    }
}

@Suppress("unused")
fun NavController.navigateToExploreNavigation() {
    navigate(Route.Main.Explore)
}