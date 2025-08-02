package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.FormattingViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.navigateToEditTextFormattingRuleDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsTextFormattingRulesDestination() {
    composable<Route.Main.Settings.TextFormatting.Rules> { navBackStackEntry ->
        val navController = LocalNavController.current
        val bookId = navBackStackEntry.toRoute<Route.Main.Settings.TextFormatting.Rules>().bookId
        val viewModel = hiltViewModel<FormattingViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
            viewModel.loadBookFormattingRules(bookId)
        }
        FormattingRulesScreen(
            rules = viewModel.rules,
            onToggle = viewModel::onToggle,
            onClickBack = navController::popBackStackIfResumed,
            onClickAddRule = { navController.navigateToEditTextFormattingRuleDialog(bookId, -1) },
            onClickEditRule = { navController.navigateToEditTextFormattingRuleDialog(bookId, it) }
        )
    }
}

fun NavController.navigateToSettingsTextFormattingRulesDestination(target: Int) {
    navigate(Route.Main.Settings.TextFormatting.Rules(target))
}
