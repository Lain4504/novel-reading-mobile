package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import javax.inject.Inject

@HiltViewModel
class PluginManagerViewModel @Inject constructor(
    @param:ApplicationContext val context: Context,
    val pluginManager: PluginManager,
    val userDataRepository: UserDataRepository
): ViewModel() {
    private val enabledPluginUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    val enabledPluginFlow = enabledPluginUserData.getFlowWithDefault(emptyList())
    val pluginList = pluginManager.allPluginInfo


    fun onClickEnabledSwitch(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            enabledPluginUserData.update {
                val list = it.toMutableList()
                if (it.contains(id)) {
                    list.remove(id)
                    CoroutineScope(Dispatchers.IO).launch {
                        pluginManager.unloadPlugin(id)
                    }
                }
                else {
                    list.add(id)
                    CoroutineScope(Dispatchers.IO).launch {
                        pluginManager.loadPlugin(id)
                    }
                }
                return@update list
            }
        }
    }

    fun installPlugin(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            val pluginFile = context.dataDir.resolve("plugin").resolve("${uri.hashCode()}.dex")
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcelFileDescriptor ->
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { plugin ->
                    pluginFile.outputStream().use {
                        plugin.copyTo(it)
                    }
                }
            }
            val id = pluginManager.loadPlugin(pluginFile, forceLoad = true)
            if (id == null) {
                viewModelScope.launch {
                    Toast
                        .makeText(context, "插件加载失败, 请检查文件合法性", Toast.LENGTH_SHORT)
                        .show()
                }
                return@launch
            }
            enabledPluginUserData.update {
                it.toMutableList().apply {
                    add(id)
                }
            }
        }
    }

    fun deletePlugin(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            pluginManager.deletePlugin(id)
        }
    }
}