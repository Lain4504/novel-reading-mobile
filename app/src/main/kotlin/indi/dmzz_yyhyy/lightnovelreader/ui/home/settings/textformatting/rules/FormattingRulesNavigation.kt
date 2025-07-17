package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.navigateToEditTextFormattingRuleDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsTextFormattingRulesDestination() {
    composable<Route.Main.Settings.TextFormatting.Rules> {
        val bookId = it.toRoute<Route.Main.Settings.TextFormatting.Rules>().bookId
        val navController = LocalNavController.current
        FormattingRulesScreen(
            bookId = bookId,
            onClickBack = navController::popBackStackIfResumed,
            onClickAddRule = {},
            onClickEditRule = navController::navigateToEditTextFormattingRuleDialog,
            onClickDeleteRule = {}
        )
    }
}

fun NavController.navigateToSettingsTextFormattingRulesDestination(target: Int) {
    navigate(Route.Main.Settings.TextFormatting.Rules(target))
}
