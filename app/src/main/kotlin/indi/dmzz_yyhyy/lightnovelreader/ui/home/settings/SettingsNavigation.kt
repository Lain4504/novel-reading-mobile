package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug.settingsDebugDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat.settingsLogcatDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Settings>(
        startDestination = Route.Main.Settings.Home
    ) {
        settingsHomeDestination(sharedTransitionScope)
        settingsDebugDestination()
        settingsLogcatDestination()
    }
}

@Suppress("unused")
fun NavController.navigateToSettingsDestination() {
    navigate(Route.Main.Settings)
}

