package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ExportUserDataDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.MutableExportContext
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SourceChangeDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.wenku8ApiWebDataSourceItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.zaiComicWebDataSourceItem
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.UpdatesAvailableDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug.navigateToSettingsDebugDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug.settingsDebugDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat.navigateToSettingsLogcatDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat.settingsLogcatDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.settingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.expandEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandExit
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopEnter
import indi.dmzz_yyhyy.lightnovelreader.utils.expandPopExit
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Settings.Home>(
        enterTransition = { expandEnter() },
        exitTransition = { expandExit() },
        popEnterTransition = { expandPopEnter() },
        popExitTransition = { expandPopExit() }
    ) {
        val settingsViewModel = hiltViewModel<SettingsViewModel>()
        val updatesAvailableDialogViewModel = hiltViewModel<UpdatesAvailableDialogViewModel>()
        val updatePhase by updatesAvailableDialogViewModel.updatePhaseFlow.collectAsState("Not Checked")
        SettingsScreen(
            controller = navController,
            selectedRoute = Route.Main.Settings,
            updatePhase = updatePhase,
            settingState = settingsViewModel.settingState,
            checkUpdate = updatesAvailableDialogViewModel::checkUpdate,
            importData = settingsViewModel::importFromFile,
            onClickDebugMode = navController::navigateToSettingsDebugDestination,
            onClickChangeSource = navController::navigateToSourceChangeDialog,
            onClickExportUserData = navController::navigateToExportUserDataDialog,
            onClickLogcat = navController::navigateToSettingsLogcatDestination,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
    sourceChangeDialog(navController)
    exportUserDataDialog(navController)
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.mainSettingsNavigation(navController: NavController, sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Settings>(
        startDestination = Route.Main.Settings.Home
    ) {
        settingsDestination(navController, sharedTransitionScope)
        settingsDebugDestination(navController)
        settingsLogcatDestination(navController)
        settingsThemeDestination(navController)
    }
}

@Suppress("unused")
fun NavController.navigateToSettingsDestination() {
    navigate(Route.Main.Settings)
}

private fun NavGraphBuilder.sourceChangeDialog(navController: NavController) {
    dialog<Route.Main.SourceChangeDialog> {
        val viewModel = hiltViewModel<SourceChangeDialogViewModel>()
        var selectedWebDataSourceId by remember { mutableIntStateOf(viewModel.webBookDataSourceId) }
        val context = LocalContext.current
        SourceChangeDialog(
            onDismissRequest = {
                navController.popBackStack()
                selectedWebDataSourceId = viewModel.webBookDataSourceId
            },
            onConfirmation = {
                viewModel.changeWebSource(selectedWebDataSourceId, File(context.filesDir, "data"))
                navController.popBackStack()
            },
            webDataSourceItems = listOf(wenku8ApiWebDataSourceItem, zaiComicWebDataSourceItem),
            selectedWebDataSourceId = selectedWebDataSourceId,
            onClickItem = {
                selectedWebDataSourceId = it
            }
        )
    }
}

private fun NavController.navigateToSourceChangeDialog() {
    navigate(Route.Main.SourceChangeDialog)
}

private fun NavGraphBuilder.exportUserDataDialog(navController: NavController) {
    dialog<Route.Main.ExportUserDataDialog> {
        val context = LocalContext.current
        val workManager = WorkManager.getInstance(context)
        val viewModel = hiltViewModel<ExportUserDataDialogViewModel>()
        var exportContext: ExportContext by remember { mutableStateOf(MutableExportContext()) }
        val saveDataToFileLauncher = uriLauncher {
            CoroutineScope(Dispatchers.Main).launch {
                workManager.getWorkInfoByIdFlow(viewModel.exportToFile(it, exportContext).id).collect {
                    when (it?.state) {
                        WorkInfo.State.FAILED -> {
                            Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
            navController.popBackStack()
        }
        ExportUserDataDialog(
            onDismissRequest = { navController.popBackStack() },
            onClickSaveAndSend = {
                viewModel.exportAndSendToFile(exportContext, context)
                navController.popBackStack()
            },
            onClickSaveToFile = {
                exportContext = it
                createDataFile("LightNovelReaderData", saveDataToFileLauncher)
            }
        )
    }
}

private fun NavController.navigateToExportUserDataDialog() {
    navigate(Route.Main.ExportUserDataDialog)
}

@Suppress("DuplicatedCode", "SameParameterValue")
private fun createDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, "$fileName.lnr")
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}
