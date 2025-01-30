package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.book.content.navigateToBookContentDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

fun NavGraphBuilder.bookDetailDestination(navController: NavController) {
    composable<Route.Book.Detail> { entry ->
        val bookId = entry.toRoute<Route.Book.Detail>().bookId
        val viewModel = hiltViewModel<DetailViewModel>()
        DetailScreen(
            onClickBackButton = {
                navController.popBackStack()
            },
            onClickChapter = {
                navController.navigateToBookContentDestination(bookId, it)
            },
            onClickReadFromStart = {
                viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                    navController.navigateToBookContentDestination(bookId, it)
                }
            },
            onClickContinueReading = {
                if (viewModel.uiState.userReadingData.lastReadChapterId == -1)
                    viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                        navController.navigateToBookContentDestination(bookId, it)
                    }
                else {
                    navController.navigateToBookContentDestination(bookId, viewModel.uiState.userReadingData.lastReadChapterId)
                }
            },
            id = bookId,
            cacheBook = {
                //FIXME
            },
            requestAddBookToBookshelf = {
                //FIXME
            },
        )
    }
}

fun NavController.navigateToBookDetailDestination(bookId: Int) {
    navigate(Route.Book.Detail(bookId))
}