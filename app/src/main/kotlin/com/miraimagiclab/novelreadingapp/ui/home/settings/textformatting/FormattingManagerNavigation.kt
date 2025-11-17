package com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.miraimagiclab.novelreadingapp.ui.dialog.EditTextFormattingRuleDialog
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.rules.settingsTextFormattingRulesDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.editTextFormattingRuleDialog() {
    dialog<Route.Main.EditTextFormattingRuleDialog> {
        val navController = LocalNavController.current
        val route = it.toRoute<Route.Main.EditTextFormattingRuleDialog>()
        val viewModel = hiltViewModel<EditTextFormattingRuleDialogViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
            viewModel.load(route.bookId, route.ruleId)
        }
        viewModel.formattingRule?.let { rule ->
            EditTextFormattingRuleDialog(
                rule = rule,
                matchTextFieldValue = viewModel.matchTextFieldValue,
                onDismissRequest = navController::popBackStack,
                onConfirmation = {
                    viewModel.onConfirmation()
                    navController.popBackStack()
                },
                onDelete = {
                    viewModel.onDelete()
                    navController.popBackStack()
                },
                onNameChange = viewModel::updateName,
                onMatchChange = viewModel::updateMatch,
                onReplacementChange = viewModel::updateReplacement,
                onIsRegexChange = viewModel::updateIsRegex
            )
        }
    }
}

fun NavController.navigateToEditTextFormattingRuleDialog(bookId: String, ruleId: Int) {
    if (!this.isResumed()) return
    navigate(Route.Main.EditTextFormattingRuleDialog(bookId, ruleId))
}

fun NavGraphBuilder.settingsTextFormattingNavigation() {
    navigation<Route.Main.Settings.TextFormatting>(
        startDestination = Route.Main.Settings.TextFormatting.Manager
    ) {
        settingsTextFormattingManagerDestination()
        settingsTextFormattingRulesDestination()
    }
}

fun NavGraphBuilder.settingsTextFormattingManagerDestination() {
    composable<Route.Main.Settings.TextFormatting.Manager> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<FormattingViewModel>()
        TextFormattingScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickGroup = navController::navigateToSettingsTextFormattingRulesDestination,
            groups = viewModel.formattingGroups,
            bookInformationMap = viewModel.bookInformationMap
        )
    }
}

fun NavController.navigateToSettingsTextFormattingManagerDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.TextFormatting.Manager)
}
