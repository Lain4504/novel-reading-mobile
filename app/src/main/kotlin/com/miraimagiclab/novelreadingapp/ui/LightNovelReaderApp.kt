package com.miraimagiclab.novelreadingapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import com.miraimagiclab.novelreadingapp.ui.dialog.UpdatesAvailableDialogViewModel
import com.miraimagiclab.novelreadingapp.ui.dialog.navigateUpdatesAvailableDialog
import com.miraimagiclab.novelreadingapp.ui.navigation.LightNovelReaderNavHost

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavGraphBuilder.() -> Unit
) {
    val navController = rememberNavController()
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    val available by updatesAvailableDialogViewModel.availableFlow.collectAsState(false)
    LaunchedEffect(available) {
        if (available) {
            updatesAvailableDialogViewModel.resetAvailable()
            navController.navigateUpdatesAvailableDialog()
        }
    }
    LightNovelReaderNavHost(navController, onBuildNavHost)
}
