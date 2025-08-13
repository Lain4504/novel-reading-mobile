package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsDebugDestination() {
    composable<Route.Main.Settings.Debug> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DebugScreenViewModel>()
        DebugScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickQuery = viewModel::runSQLCommand,
            result = viewModel.result
        )
    }
}

fun NavController.navigateToSettingsDebugDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.Debug)
}