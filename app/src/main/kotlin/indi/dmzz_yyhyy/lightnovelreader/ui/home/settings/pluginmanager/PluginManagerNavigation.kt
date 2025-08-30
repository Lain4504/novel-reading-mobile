package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.navigateToSettingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.settingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher

fun NavGraphBuilder.settingsPluginManagerNavigation() {
    navigation<Route.Main.Settings.PluginManager>(
        startDestination = Route.Main.Settings.PluginManager.Home
    ) {
        settingsPluginManagerHomeDestination()
        settingsPluginManagerDetailDestination()
    }
}


fun NavGraphBuilder.settingsPluginManagerHomeDestination() {
    composable<Route.Main.Settings.PluginManager.Home> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginManagerViewModel>()
        val launcher = uriLauncher {
            viewModel.installPlugin(it)
        }
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsState(emptyList())
        PluginManagerScreen(
            enabledPluginList = enabledPluginList,
            onClickBack = navController::popBackStackIfResumed,
            onClickDetail = navController::navigateToSettingsPluginManagerDetailDestination,
            onClickSwitch = viewModel::onClickEnabledSwitch,
            onClickDelete = viewModel::deletePlugin,
            pluginInfoList = viewModel.pluginList,
            onClickInstall = {
                val initUri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Documents"
                )
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
                }
                launcher.launch(Intent.createChooser(intent, "选择扩展插件"))
            }
        )
    }
}

fun NavController.navigateToSettingsPluginManagerHomeDestination() {
    navigate(Route.Main.Settings.PluginManager.Home)
}
