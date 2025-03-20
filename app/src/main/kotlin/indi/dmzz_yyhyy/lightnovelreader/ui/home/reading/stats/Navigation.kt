package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.navigateToReadingStatsDailyDetailedDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.readingStatsDetailedDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.readingStatsNavigation(navController: NavController) {
    navigation<Route.Main.Reading.Stats>(
        enterTransition = { expandEnter() },
        exitTransition = { expandExit() },
        popEnterTransition = { expandPopEnter() },
        popExitTransition = { expandPopExit() },
        startDestination = Route.Main.Reading.Stats.Overview
    ) {
        readingStatsOverviewDestination(navController)
        readingStatsDetailedDestination(navController)
    }
}

fun NavGraphBuilder.readingStatsOverviewDestination(navController: NavController) {
    composable<Route.Main.Reading.Stats.Overview> {
        val statsOverviewViewModel = hiltViewModel<StatsOverviewViewModel>()
        StatsOverviewScreen(
            onClickBack = navController::popBackStackIfResumed,
            viewModel = statsOverviewViewModel,
            onClickDetail = navController::navigateToReadingStatsDailyDetailedDestination
        )
    }
}

fun NavController.navigateToReadingStatsDestination() {
    navigate(Route.Main.Reading.Stats)
}
