package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.bookshelfEditDestination(navController: NavController) {
    composable<Route.Home.Bookshelf.Edit> {
        val editBookshelfViewModel = hiltViewModel<EditBookshelfViewModel>()
        val edit = it.toRoute<Route.Home.Bookshelf.Edit>()
        EditBookshelfScreen(
            title = edit.title,
            bookshelfId = edit.id,
            bookshelf = editBookshelfViewModel.uiState,
            dialog = {
                //FIXME
            },
            inti = editBookshelfViewModel::init,
            onClickBack = { navController.popBackStack() },
            onClickSave = {
                navController.popBackStack()
                editBookshelfViewModel.save()
            },
            onClickDelete = {
                navController.popBackStack()
                editBookshelfViewModel.delete()
            },
            onNameChange = editBookshelfViewModel::onNameChange,
            onAutoCacheChange = editBookshelfViewModel::onAutoCacheChange,
            onSystemUpdateReminderChange = editBookshelfViewModel::onSystemUpdateReminderChange,
        )
    }
}

fun NavController.navigateToBookshelfEditDestination(id: Int, title: String) {
    navigate(Route.Home.Bookshelf.Edit(id, title))
}