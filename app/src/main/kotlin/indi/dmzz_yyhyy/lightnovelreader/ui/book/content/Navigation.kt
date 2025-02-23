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
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
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
            onClickChangeBackgroundColor = { navController.navigateToColorPickerDialog(
                UserDataPath.Reader.BackgroundColor.path,
                listOf(-1, 0x38E8CCA5, 0x38FF8080, 0x38d3b17d,0x3834C759, 0x3832ADE6, 0x38007AFF, 0x385856D6, 0x38AF52DE)
            ) },
            onClickChangeTextColor = { navController.navigateToColorPickerDialog(
                UserDataPath.Reader.TextColor.path,
                listOf(-1, 0xFF1D1B20, 0xFFE6E0E9)
            ) }
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
        val route = entry.toRoute<Route.Book.ColorPickerDialog>()
        val selectedColor by viewModel.init(route.colorUserDataPath).collectAsState(Color.Unspecified)
        ColorPickerDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.changeBackgroundColor(it)
                navController.popBackStack()
            },
            selectedColor = selectedColor ?: Color.Unspecified,
            colors = route.colors.map { Color(if (it < 0) return@map Color.Unspecified else it) }
        )
    }
}

private fun NavController.navigateToColorPickerDialog(colorUserDataPath: String, colors: List<Long>) {
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray()))
}