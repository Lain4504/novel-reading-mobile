package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.bookReaderDestination() {
    composable<Route.Book.Reader> { navBackStackEntry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ReaderViewModel>(navBackStackEntry)
        ReaderScreen(
            readingScreenUiState = viewModel.uiState,
            settingState = viewModel.settingState,
            onClickBackButton = navController::popBackStackIfResumed,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            accumulateReadTime = viewModel::accumulateReadingTime,
            onClickLastChapter = viewModel::lastChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination
        )
    }
    colorPickerDialog()
}

fun NavController.navigateToBookReaderDestination(bookId: Int, chapterId: Int, context: Context) {
    this.navigate(Route.Book.Reader)
    val entry = this.getBackStackEntry<Route.Book.Reader>()
    val viewModel = ViewModelProvider.create(
        entry,
        HiltViewModelFactory(
            context = context,
            delegateFactory = entry.defaultViewModelProviderFactory
        ),
    )[ReaderViewModel::class.java]
    viewModel.bookId = bookId
    viewModel.changeChapter(chapterId)
}

private fun NavGraphBuilder.colorPickerDialog() {
    dialog<Route.Book.ColorPickerDialog> { entry ->
        val navController = LocalNavController.current
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

fun NavController.navigateToColorPickerDialog(colorUserDataPath: String, colors: List<Long>) {
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray()))
}