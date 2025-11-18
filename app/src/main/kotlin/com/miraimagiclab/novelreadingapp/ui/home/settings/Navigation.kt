package com.miraimagiclab.novelreadingapp.ui.home.settings

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.miraimagiclab.novelreadingapp.ui.components.ExportContext
import com.miraimagiclab.novelreadingapp.ui.components.ExportUserDataDialog
import com.miraimagiclab.novelreadingapp.ui.components.MutableExportContext
import com.miraimagiclab.novelreadingapp.ui.components.SliderValueDialog
import com.miraimagiclab.novelreadingapp.ui.dialog.SliderValueDialogViewModel
import com.miraimagiclab.novelreadingapp.ui.home.settings.logcat.navigateToSettingsLogcatDestination
import com.miraimagiclab.novelreadingapp.ui.home.settings.logcat.settingsLogcatDestination
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.editTextFormattingRuleDialog
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.navigateToSettingsTextFormattingManagerDestination
import com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting.settingsTextFormattingNavigation
import com.miraimagiclab.novelreadingapp.ui.home.settings.theme.navigateToSettingsThemeDestination
import com.miraimagiclab.novelreadingapp.ui.home.settings.theme.settingsThemeDestination
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import com.miraimagiclab.novelreadingapp.utils.isResumed
import com.miraimagiclab.novelreadingapp.utils.uriLauncher
import io.lain4504.novelreadingapp.api.ui.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Settings.Home> {
        val navController = LocalNavController.current
        val settingsViewModel = hiltViewModel<SettingsViewModel>()
        SettingsScreen(
            controller = navController,
            selectedRoute = Route.Main.Settings,
            settingState = settingsViewModel.settingState,
            importData = settingsViewModel::importFromFile,
            onClickExportUserData = navController::navigateToExportUserDataDialog,
            onClickLogcat = navController::navigateToSettingsLogcatDestination,
            onClickTextFormatting = navController::navigateToSettingsTextFormattingManagerDestination,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination,
            animatedVisibilityScope = this,
            sharedTransitionScope = sharedTransitionScope
        )
    }
    exportUserDataDialog()
    editTextFormattingRuleDialog()
    sliderValueDialog()
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.settingsNavigation(sharedTransitionScope: SharedTransitionScope) {
    navigation<Route.Main.Settings>(
        startDestination = Route.Main.Settings.Home
    ) {
        settingsDestination(sharedTransitionScope)
        settingsLogcatDestination()
        settingsThemeDestination()
        settingsTextFormattingNavigation()
    }
}

@Suppress("unused")
fun NavController.navigateToSettingsDestination() {
    navigate(Route.Main.Settings)
}

private fun NavGraphBuilder.sliderValueDialog() {
    dialog<Route.SliderValueDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<SliderValueDialogViewModel>()
        val route = entry.toRoute<Route.SliderValueDialog>()
        val value = route.value
        SliderValueDialog(
            value = value,
            onValueChange = { viewModel.setValue(it) },
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                navController.popBackStack()
            }
        )

    }
}

fun NavController.navigateToSliderValueDialog(path: String, value: Float) {
    if (!this.isResumed()) return
    navigate(Route.SliderValueDialog(value, path))
}


private fun NavGraphBuilder.exportUserDataDialog() {
    dialog<Route.Main.ExportUserDataDialog> {
        val navController = LocalNavController.current
        val context = LocalContext.current
        val workManager = WorkManager.getInstance(context)
        val viewModel = hiltViewModel<ExportUserDataDialogViewModel>()
        var exportContext: ExportContext by remember { mutableStateOf(MutableExportContext()) }
        val saveDataToFileLauncher = uriLauncher { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                workManager.getWorkInfoByIdFlow(viewModel.exportToFile(uri, exportContext).id)
                    .collect {
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
