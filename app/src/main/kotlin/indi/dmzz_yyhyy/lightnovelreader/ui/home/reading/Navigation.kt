package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.readingStatsDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import readingHomeDestination

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeReadingDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Reading> {

    navigation<Route.Home.Reading>(
        startDestination = Route.Home.Reading.Home
    ) {
        readingHomeDestination(navController, sharedTransitionScope)
        readingStatsDestination(navController)
    }
}

@Suppress("unused")
fun NavController.navigateToHomeReadingDestination() {
    navigate(Route.Home.Reading)
}