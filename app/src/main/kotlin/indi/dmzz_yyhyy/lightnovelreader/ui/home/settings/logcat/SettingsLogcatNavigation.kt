package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsLogcatDestination() {
    composable<Route.Main.Settings.Logcat> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<LogcatViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            viewModel.startLogging()
        }
        LogcatScreen(
            logEntries = viewModel.logEntries,
            onClickBack = navController::popBackStackIfResumed,
            onClickClearLogs = viewModel::clearLogs,
            onClickShareLogs = viewModel::shareLogs,
        )
    }
}


fun NavController.navigateToSettingsLogcatDestination() {
    navigate(Route.Main.Settings.Logcat)
}

