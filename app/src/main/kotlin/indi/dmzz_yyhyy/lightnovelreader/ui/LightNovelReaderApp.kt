package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import indi.dmzz_yyhyy.lightnovelreader.data.update.ReleaseStatus
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateUpdatesAvailableDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.LightNovelReaderNavHost

@Composable
fun LightNovelReaderApp() {
    val navController = rememberNavController()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    LaunchedEffect(updatesAvailableDialogViewModel.release) {
        if (updatesAvailableDialogViewModel.release?.status == ReleaseStatus.AVAILABLE)
            navController.navigateUpdatesAvailableDialog()
    }
    LightNovelReaderNavHost(navController)
}
