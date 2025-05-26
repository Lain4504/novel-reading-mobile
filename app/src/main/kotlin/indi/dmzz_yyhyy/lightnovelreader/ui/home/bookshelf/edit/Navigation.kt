package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.bookshelfEditDestination() {
    composable<Route.Main.Bookshelf.Edit>(

    ) {
        val navController = LocalNavController.current
        val editBookshelfViewModel = hiltViewModel<EditBookshelfViewModel>()
        val edit = it.toRoute<Route.Main.Bookshelf.Edit>()
        EditBookshelfScreen(
            title = edit.title,
            bookshelfId = edit.id,
            bookshelf = editBookshelfViewModel.uiState,
            init = editBookshelfViewModel::init,
            onClickBack = navController::popBackStackIfResumed,
            onClickSave = {
                navController.popBackStackIfResumed()
                editBookshelfViewModel.save()
            },
            onClickDelete = navController::navigateToDeleteBookshelfDialog,
            onNameChange = editBookshelfViewModel::onNameChange,
            onAutoCacheChange = editBookshelfViewModel::onAutoCacheChange,
            onSystemUpdateReminderChange = editBookshelfViewModel::onSystemUpdateReminderChange,
        )
    }
    deleteBookshelfDialog()
}

fun NavController.navigateToBookshelfEditDestination(id: Int, title: String) {
    navigate(Route.Main.Bookshelf.Edit(id, title))
}

private fun NavGraphBuilder.deleteBookshelfDialog() {
    dialog<Route.Main.Bookshelf.DeleteBookshelfDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DeleteBookshelfDialogViewModel>()
        DeleteBookshelfDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.deleteBookshelf(it.toRoute<Route.Main.Bookshelf.DeleteBookshelfDialog>().bookshelfId)
                navController.popBackStack()
                navController.popBackStackIfResumed()
            }
        )
    }
}

private fun NavController.navigateToDeleteBookshelfDialog(bookId: Int) {
    navigate(Route.Main.Bookshelf.DeleteBookshelfDialog(bookId))
}

@Composable
private fun DeleteBookshelfDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf_text),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = stringResource(android.R.string.ok),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

