package com.miraimagiclab.novelreadingapp.ui.home.bookshelf.edit

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.miraimagiclab.novelreadingapp.ui.components.DeleteBookshelfDialog
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

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
    if (!this.isResumed()) return
    navigate(Route.Main.Bookshelf.DeleteBookshelfDialog(bookId))
}
