package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.homeSettingDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Settings> {
        val settingsViewModel = hiltViewModel<SettingsViewModel>()
        SettingsScreen(
            controller = navController,
            selectedRoute = Route.Home.Settings,
            settingState = settingsViewModel.settingState,
            exportDataToFile = settingsViewModel::exportToFile,
            exportAndSendToFile = settingsViewModel::exportAndSendToFile,
            importData = settingsViewModel::importFromFile,
            changeWebDataSource = settingsViewModel::changeWebSource,
            webDataSourceId = settingsViewModel.webBookDataSourceId,
            dialog = {
                //FIXME
            },
            checkUpdate = {
                //FIXME
            },
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

@Suppress("unused")
fun NavController.navigateToHomeSettingDestination() {
    navigate(Route.Home.Settings)
}