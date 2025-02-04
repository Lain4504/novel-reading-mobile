package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.readingStatsDestination(navController: NavController) {
    composable<Route.Home.Reading.Stats> {
        val readingStatsViewModel = hiltViewModel<ReadingStatsViewModel>()
        ReadingStatsScreen(
            onClickBack = navController::popBackStack,
            viewModel = readingStatsViewModel
        )
    }
}

fun NavController.navigateToReadingStatsDestination() {
    navigate(Route.Home.Reading.Stats)
}