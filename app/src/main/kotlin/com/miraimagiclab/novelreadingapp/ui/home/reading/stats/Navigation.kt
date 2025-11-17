package com.miraimagiclab.novelreadingapp.ui.home.reading.stats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.home.reading.stats.detailed.navigateToReadingStatsDetailedDestination
import com.miraimagiclab.novelreadingapp.ui.home.reading.stats.detailed.readingStatsDetailedDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.readingStatsNavigation() {
    navigation<Route.Main.Reading.Stats>(
        startDestination = Route.Main.Reading.Stats.Overview
    ) {
        readingStatsOverviewDestination()
        readingStatsDetailedDestination()
    }
}

fun NavGraphBuilder.readingStatsOverviewDestination() {
    composable<Route.Main.Reading.Stats.Overview> {
        val navController = LocalNavController.current
        val statsOverviewViewModel = hiltViewModel<StatsOverviewViewModel>()
        StatsOverviewScreen(
            onClickBack = navController::popBackStackIfResumed,
            viewModel = statsOverviewViewModel,
            onClickDetailScreen = navController::navigateToReadingStatsDetailedDestination
        )
    }
}

fun NavController.navigateToReadingStatsDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Reading.Stats)
}
