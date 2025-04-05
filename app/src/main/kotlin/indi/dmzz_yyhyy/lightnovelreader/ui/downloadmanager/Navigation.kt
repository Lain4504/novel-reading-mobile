package indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.downloadManager() {
    composable<Route.DownloadManager> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DownloadManagerViewModel>()
        DownloadManagerScreen(
            downloadItemIdList = viewModel.downloadItemIdList,
            bookInformationMap = viewModel.bookInformationMap,
            onClickBack = navController::popBackStackIfResumed,
            onClickCancel = viewModel::onClickCancel,
            onClickClearCompleted = viewModel::onClickClearCompleted
        )
    }
}

fun NavController.navigateToDownloadManager() {
    navigate(Route.DownloadManager)
}