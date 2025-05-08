package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.navigateToColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsThemeDestination(navController: NavController) {
    composable<Route.Main.Settings.Theme>(
        enterTransition = { expandEnter() },
        exitTransition = { expandExit() },
        popEnterTransition = { expandPopEnter() },
        popExitTransition = { expandPopExit() }
    ) {
        val viewModel = hiltViewModel<ThemeViewModel>()
        val themeSettingState = viewModel.settingState
        ThemeScreen(
            themeSettingState = themeSettingState,
            onClickBack = navController::popBackStackIfResumed,
            onClickChangeTextColor = {
                navController.navigateToColorPickerDialog(
                    UserDataPath.Reader.TextColor.path,
                    listOf(-1, 0xFF1D1B20, 0xFFE6E0E9)
                )
            }
        )
    }
}

@Suppress("unused")
fun NavController.navigateToSettingsThemeDestination() {
    navigate(Route.Main.Settings.Theme)
}

