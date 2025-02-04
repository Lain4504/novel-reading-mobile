package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.bookContentDestination(navController: NavController) {
    composable<Route.Book.Content> {
        val viewModel = hiltViewModel<ContentViewModel>()
        ContentScreen(
            onClickBackButton = navController::popBackStack,
            bookId = it.toRoute<Route.Book.Content>().bookId,
            chapterId = it.toRoute<Route.Book.Content>().chapterId,
            uiState = viewModel.uiState,
            settingState = viewModel.settingState,
            addToReadingBook = viewModel::addToReadingBook,
            init = viewModel::init,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            updateReadingStats = viewModel::updateReadingProgress,
            onClickLastChapter = viewModel::lastChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onChapterReadingProgressChange = viewModel::changeChapterReadingProgress
        )
    }
}

fun NavController.navigateToBookContentDestination(bookId: Int, chapterId: Int) {
    navigate(Route.Book.Content(bookId, chapterId))
}