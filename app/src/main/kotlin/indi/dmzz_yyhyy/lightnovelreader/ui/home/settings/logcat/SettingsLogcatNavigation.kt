package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsLogcatDestination(navController: NavController) {
    composable<Route.Main.Settings.Logcat> {
        val viewModel = hiltViewModel<LogcatViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            viewModel.startLogging()
        }
        val logEntries by remember { derivedStateOf { viewModel.displayedLogEntries } }
        LogcatScreen(
            uiState = viewModel.uiState,
            logFiles = viewModel.logFilenameList,
            logEntries = logEntries,
            onClickBack = navController::popBackStackIfResumed,
            onClickClearLogs = viewModel::clearLogs,
            onClickShareLogs = viewModel::shareLogs,
            onSelectLogFile = viewModel::onSelectLogFile
        )
    }
}


fun NavController.navigateToSettingsLogcatDestination() {
    navigate(Route.Main.Settings.Logcat)
}

