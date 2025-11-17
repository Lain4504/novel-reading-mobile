package com.miraimagiclab.novelreadingapp.ui.home.settings.debug

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.book.detail.navigateToBookDetailDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.settingsDebugDestination() {
    composable<Route.Main.Settings.Debug> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DebugScreenViewModel>()
        DebugScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickQuery = viewModel::runSQLCommand,
            onClickOpenBook = {
                navController.navigateToBookDetailDestination(it)
            },
            result = viewModel.result
        )
    }
}

fun NavController.navigateToSettingsDebugDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.Debug)
}