package com.miraimagiclab.novelreadingapp.ui.home.reading

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.home.reading.home.readingHomeDestination
import com.miraimagiclab.novelreadingapp.ui.home.reading.stats.readingStatsNavigation
import com.miraimagiclab.novelreadingapp.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.readingNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Reading>(
        startDestination = Route.Main.Reading.Home,
    ) {
        readingHomeDestination(sharedTransitionScope)
        readingStatsNavigation()
    }
}

@Suppress("unused")
fun NavController.navigateToHomeReadingDestination() {
    navigate(Route.Main.Reading)
}