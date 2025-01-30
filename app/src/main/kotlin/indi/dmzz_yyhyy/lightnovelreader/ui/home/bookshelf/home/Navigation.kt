package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.navigateToBookshelfEditDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.bookshelfHomeDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Home.Bookshelf.Home> {
        val bookshelfHomeViewModel = hiltViewModel<BookshelfHomeViewModel>()
        BookshelfHomeScreen(
            controller = navController,
            selectedRoute = Route.Home.Bookshelf,
            init = bookshelfHomeViewModel::load,
            dialog = {
                //FIXME
            },
            changePage = bookshelfHomeViewModel::changePage,
            changeBookSelectState = bookshelfHomeViewModel::changeBookSelectState,
            uiState = bookshelfHomeViewModel.uiState,
            onClickCreate = {
                navController.navigateToBookshelfEditDestination(-1, "新建书架")
            },
            onClickEdit = {
                navController.navigateToBookshelfEditDestination(it, "编辑书架")
            },
            onClickBook = {
                //FIXME
            },
            onClickEnableSelectMode = bookshelfHomeViewModel::enableSelectMode,
            onClickDisableSelectMode = bookshelfHomeViewModel::disableSelectMode,
            onClickSelectAll = bookshelfHomeViewModel::selectAllBooks,
            onClickPin = bookshelfHomeViewModel::pinSelectedBooks,
            onClickRemove = bookshelfHomeViewModel::removeSelectedBooks,
            saveAllBookshelfJsonData = bookshelfHomeViewModel::saveAllBookshelfJsonData,
            saveBookshelfJsonData = bookshelfHomeViewModel::saveThisBookshelfJsonData,
            importBookshelf = bookshelfHomeViewModel::importBookshelf,
            markSelectedBooks = bookshelfHomeViewModel::markSelectedBooks,
            clearToast = bookshelfHomeViewModel::clearToast,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

@Suppress("unused")
fun NavController.navigateToBookshelfHomeDestination() {
    navigate(Route.Home.Bookshelf.Home)
}