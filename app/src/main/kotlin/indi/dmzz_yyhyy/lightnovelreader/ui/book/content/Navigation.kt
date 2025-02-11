package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.bookContentDestination(navController: NavController) {
    composable<Route.Book.Content> {
        val viewModel = hiltViewModel<ContentViewModel>()
        ContentScreen(
            onClickBackButton = navController::popBackStackIfResumed,
            bookId = it.toRoute<Route.Book.Content>().bookId,
            chapterId = it.toRoute<Route.Book.Content>().chapterId,
            uiState = viewModel.uiState,
            settingState = viewModel.settingState,
            addToReadingBook = viewModel::addToReadingBook,
            init = viewModel::init,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            onClickLastChapter = viewModel::lastChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onChapterReadingProgressChange = viewModel::changeChapterReadingProgress,
            onClickChangeBackgroundColor = { navController.navigateToColorPickerDialog(viewModel.settingState.backgroundColor) }
        )
    }
    colorPickerDialog(navController)
}

fun NavController.navigateToBookContentDestination(bookId: Int, chapterId: Int) {
    navigate(Route.Book.Content(bookId, chapterId))
}

private fun NavGraphBuilder.colorPickerDialog(navController: NavController) {
    dialog<Route.Book.ColorPickerDialog> { entry ->
        val viewModel = hiltViewModel<ColorPickerDialogViewModel>()
        val selectedColor by viewModel.selectedColorFlow.collectAsState(Color.Unspecified)
        ColorPickerDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.changeBackgroundColor(it)
                navController.popBackStack()
            },
            selectedColor = selectedColor ?: Color.Unspecified
        )
    }
}

private fun NavController.navigateToColorPickerDialog(selectedColor: Color) {
    navigate(Route.Book.ColorPickerDialog)
}
