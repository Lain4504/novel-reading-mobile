package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.EditTextFormattingRuleDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.settingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.editTextFormattingRuleDialog() {
    dialog<Route.Main.EditTextFormattingRuleDialog> {
        val navController = LocalNavController.current
        val route = it.toRoute<Route.Main.EditTextFormattingRuleDialog>()
        val ruleId = route.ruleId
        EditTextFormattingRuleDialog(
            onDismissRequest = navController::popBackStack,
            onConfirmation = { },
            onDelete = { },
        )
    }
}

fun NavController.navigateToEditTextFormattingRuleDialog(ruleId: Int) {
    navigate(Route.Main.EditTextFormattingRuleDialog(ruleId))
}

fun NavGraphBuilder.settingsTextFormattingNavigation() {
    navigation<Route.Main.Reading.Stats>(
        startDestination = Route.Main.Settings.TextFormatting.Manager
    ) {
        settingsTextFormattingManagerDestination()
        settingsTextFormattingRulesDestination()
    }
}

fun NavGraphBuilder.settingsTextFormattingManagerDestination() {
    composable<Route.Main.Settings.TextFormatting.Manager> {
        val navController = LocalNavController.current
        TextFormattingScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickGroup = navController::navigateToSettingsTextFormattingRulesDestination
        )
    }
}

fun NavController.navigateToSettingsTextFormattingManagerDestination() {
    navigate(Route.Main.Settings.TextFormatting.Manager)
}
