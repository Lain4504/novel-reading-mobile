package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsLogcatDestination(navController: NavController) {
    composable<Route.Main.Settings.Logcat> {
        val viewModel = hiltViewModel<LogcatViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            viewModel.startLogging()
        }
        LogcatScreen(
            uiState = viewModel.uiState,
            onClickBack = navController::popBackStackIfResumed,
            onClickClearLogs = viewModel::clearLogs,
            onClickShareLogs = viewModel::shareLogs,
        )
    }
}


fun NavController.navigateToSettingsLogcatDestination() {
    navigate(Route.Main.Settings.Logcat)
}

