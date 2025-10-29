package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    private val pluginManager: PluginManager,
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val enabledPluginUserData =
        userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)

    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())
    val pluginList = pluginManager.allPluginInfo

    private val _snackbarFlow = MutableSharedFlow<String>()
    val snackbarFlow = _snackbarFlow.asSharedFlow()

    fun onClickEnabledSwitch(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            enabledPluginUserData.update { current ->
                val list = current.toMutableList()
                if (current.contains(id)) {
                    list.remove(id)
                    runCatching { pluginManager.unloadPlugin(id) }
                } else {
                    list.add(id)
                    runCatching { pluginManager.loadPlugin(id) }
                }
                list
            }
        }
    }

    fun showSnackbar(message: String) {
        viewModelScope.launch(Dispatchers.Main) { _snackbarFlow.emit(message) }
    }

    @Composable
    fun PluginContent(id: String, paddingValues: PaddingValues) {
        pluginManager.PluginContent(id, paddingValues)
    }
}