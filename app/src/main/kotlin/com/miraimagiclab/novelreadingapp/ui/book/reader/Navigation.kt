package com.miraimagiclab.novelreadingapp.ui.book.reader

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.HiltViewModelFactory
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.miraimagiclab.novelreadingapp.ui.book.reader.imageview.ImageViewerScreen
import com.miraimagiclab.novelreadingapp.ui.book.reader.imageview.ImageViewerViewModel
import com.miraimagiclab.novelreadingapp.ui.components.ColorPickerDialog
import com.miraimagiclab.novelreadingapp.ui.home.settings.theme.navigateToSettingsThemeDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import com.miraimagiclab.novelreadingapp.R
import com.miraimagiclab.novelreadingapp.utils.ImageUtils.saveBitmapAsPng
import com.miraimagiclab.novelreadingapp.utils.ImageUtils.uriToBitmap
import io.lain4504.novelreadingapp.api.ui.LocalNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun NavGraphBuilder.bookReaderDestination() {
    composable<Route.Book.Reader> { navBackStackEntry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ReaderViewModel>(navController.getBackStackEntry<Route.Book>())
        ReaderScreen(
            readingScreenUiState = viewModel.uiState,
            settingState = viewModel.settingState,
            onClickBackButton = navController::popBackStackIfResumed,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            accumulateReadTime = viewModel::accumulateReadingTime,
            onClickPrevChapter = viewModel::prevChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination
        )
    }
    colorPickerDialog()
    imageViewerDialog()
}

fun NavController.navigateToBookReaderDestination(bookId: String, chapterId: String, context: Context) {
    if (this.currentBackStackEntry?.destination?.route?.contains("com.miraimagiclab.novelreadingapp.ui.navigation.Route.Book.Detail") != true) {
        return
    }
    val entry = this.getBackStackEntry<Route.Book>()
    val viewModel = ViewModelProvider.create(
        entry,
        HiltViewModelFactory(
            context = context,
            delegateFactory = entry.defaultViewModelProviderFactory
        ),
    )[ReaderViewModel::class.java]
    viewModel.bookId = bookId
    viewModel.changeChapter(chapterId)
    this.navigate(Route.Book.Reader)
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
    if (!this.isResumed()) return
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray()))
}
private fun NavGraphBuilder.imageViewerDialog() {
    dialog<Route.Book.ImageViewerDialog>(
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) { entry ->
        val navController = LocalNavController.current
        val route = entry.toRoute<Route.Book.ImageViewerDialog>()
        val viewModel = hiltViewModel<ImageViewerViewModel>()

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        ImageViewerScreen(
            imageUri = route.imageUri.toUri(),
            onDismissRequest = { navController.popBackStack() },
            onClickSave = {
                coroutineScope.launch(Dispatchers.IO) {
                    uriToBitmap(
                        imageUri = route.imageUri.toUri(),
                        context = context,
                        header = viewModel.imageHeader
                    )
                        .onSuccess {
                            coroutineScope.launch {
                                saveBitmapAsPng(context, it)
                                    .onSuccess {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.saved_to_pictures_dir, it),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.save_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .onFailure {
                            Log.d("ImageViewer", "Failed to save image: ${it.message}")
                            Toast.makeText(
                                context,
                                context.getString(R.string.save_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            header = viewModel.imageHeader
        )
    }
}

fun NavController.navigateToImageViewerDialog(
    imageUri: Uri
) {
    navigate(
        Route.Book.ImageViewerDialog(
            imageUri = imageUri.toString()
        )
    )
}