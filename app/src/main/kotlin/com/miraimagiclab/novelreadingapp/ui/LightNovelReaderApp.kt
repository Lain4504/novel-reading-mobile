package com.miraimagiclab.novelreadingapp.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.rememberNavController
import com.miraimagiclab.novelreadingapp.R
import com.miraimagiclab.novelreadingapp.ui.dialog.UpdatesAvailableDialogViewModel
import com.miraimagiclab.novelreadingapp.ui.navigation.LightNovelReaderNavHost
import com.miraimagiclab.novelreadingapp.utils.showSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun LightNovelReaderApp(
    onBuildNavHost: NavGraphBuilder.() -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = remember { CoroutineScope(Dispatchers.Main) }
    val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
    
    val updateDownloaded by updatesAvailableDialogViewModel.updateDownloaded.collectAsState(false)
    
    // Show snackbar when update is downloaded
    LaunchedEffect(updateDownloaded) {
        if (updateDownloaded) {
            showSnackbar(
                coroutineScope = coroutineScope,
                hostState = snackbarHostState,
                message = context.getString(R.string.update_downloaded),
                actionLabel = context.getString(R.string.restart_app),
                duration = SnackbarDuration.Long
            ) { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    updatesAvailableDialogViewModel.completeUpdate()
                }
            }
        }
    }
    
    LightNovelReaderNavHost(navController, onBuildNavHost)
}
