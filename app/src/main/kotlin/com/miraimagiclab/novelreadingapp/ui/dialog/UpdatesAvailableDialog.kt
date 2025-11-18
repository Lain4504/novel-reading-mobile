package com.miraimagiclab.novelreadingapp.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.updatesAvailableDialog() {
    dialog<Route.UpdatesAvailableDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        UpdatesAvailableDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = { viewModel.completeUpdate() }
        )
    }
}

@Composable
fun UpdatesAvailableDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_update_ready),
                style = AppTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(text = stringResource(R.string.dialog_update_ready_desc))
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.restart_app))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.later))
            }
        }
    )
}

fun NavController.navigateUpdatesAvailableDialog() {
    navigate(Route.UpdatesAvailableDialog)
}
