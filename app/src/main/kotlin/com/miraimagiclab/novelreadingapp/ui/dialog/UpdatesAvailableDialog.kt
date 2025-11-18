package com.miraimagiclab.novelreadingapp.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.miraimagiclab.novelreadingapp.data.update.Release
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.miraimagiclab.novelreadingapp.BuildConfig
import com.miraimagiclab.novelreadingapp.R
import io.lain4504.novelreadingapp.api.ui.theme.AppTypography
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.updatesAvailableDialog() {
    dialog<Route.UpdatesAvailableDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        UpdatesAvailableDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = { viewModel.downloadUpdate() },
            release = viewModel.release
        )
    }
}

@Composable
fun UpdatesAvailableDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    release: Release?
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_updates_available),
                style = AppTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column {
                release?.versionName?.let {
                    Text(
                        text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) → ${release.versionName}(${release.version})"
                    )
                }
                release?.releaseNotes?.let {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .wrapContentHeight()
                            .heightIn(max = 350.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.changelog),
                                style = AppTypography.titleMedium,
                            )
                        }
                        item {
                            MarkdownText(it)
                        }
                    }
                } ?: Spacer(modifier = Modifier.height(12.dp))
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(R.string.open_play_store))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.decline))
            }
        }
    )
}

fun NavController.navigateUpdatesAvailableDialog() {
    navigate(Route.UpdatesAvailableDialog)
}
