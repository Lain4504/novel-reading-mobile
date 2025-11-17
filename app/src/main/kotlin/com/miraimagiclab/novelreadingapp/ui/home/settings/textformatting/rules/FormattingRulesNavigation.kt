package com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.rules

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.FormattingViewModel
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.navigateToEditTextFormattingRuleDialog
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

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

fun NavController.navigateToSettingsTextFormattingRulesDestination(target: String) {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.TextFormatting.Rules(target))
}
