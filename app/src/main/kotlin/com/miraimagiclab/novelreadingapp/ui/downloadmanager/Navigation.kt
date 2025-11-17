package com.miraimagiclab.novelreadingapp.ui.downloadmanager

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.popBackStackIfResumed
import io.lain4504.novelreadingapp.api.ui.LocalNavController

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
    if (!this.isResumed()) return
    navigate(Route.DownloadManager)
}