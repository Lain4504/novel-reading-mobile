package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor(
    @field:ApplicationContext private val appContext: Context,
    private val webBookDataSourceManager: WebBookDataSourceManager,
    userDataRepository: UserDataRepository
) {
    private val _allPluginInfo = mutableStateListOf<PluginInfo>()
    private val pluginPathMap = mutableMapOf<String, File>()
    val allPluginInfo: SnapshotStateList<PluginInfo> = _allPluginInfo
    private val enabledPluginsUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    private val defaultWebDataSources = listOf(Wenku8Api, ZaiComic)
    private val defaultPlugins = listOf<LightNovelReaderPlugin>()

    fun loadAllPlugins() {
        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        defaultPlugins.forEach(::loadPlugin)
        appContext.dataDir.resolve("plugin")
            .also(File::mkdir)
            .listFiles { it.name.endsWith(".dex") }
            ?.forEach(::loadPlugin)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin): String? {
        var id: String?
        val annotation = plugin.javaClass.getAnnotation(Plugin::class.java)
        if (annotation != null) {
            id = plugin.javaClass.`package`?.name ?: return null
            val info = PluginInfo(
                isUpdatable = false,
                id = id,
                name = annotation.name,
                version = annotation.version,
                versionName = annotation.versionName,
                author = annotation.author,
                description = annotation.description,
                updateUrl = annotation.updateUrl
            )
            if (!_allPluginInfo.contains(info)) _allPluginInfo.add(info)
            return id
        }
        return null
    }

    fun loadPlugin(path: File): String? {
        val classLoader = DexClassLoader(
            path.path,
            appContext.cacheDir.resolve("plugin/optimizedDirectory").path,
            null,
            appContext.classLoader
        )
        return loadPlugin(
            classLoader
        ).also {
            if (it != null) {
                pluginPathMap[it] = path
            }
        }
    }

    fun loadPlugin(classLoader: DexClassLoader): String? {
        var id: String? = null
        AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java)
            .map {
                try { it.getDeclaredField("INSTANCE").get(null) }
                catch (_: NoSuchFieldException) { null }
                    ?: it.getDeclaredConstructor().newInstance()
            }
            .filter { it is LightNovelReaderPlugin }
            .map { it as LightNovelReaderPlugin }
            .firstOrNull()
            ?.also {
                id = loadPlugin(it)
                if (!enabledPluginsUserData.getOrDefault(emptyList()).contains(id)) return id
            }
            ?.let(LightNovelReaderPlugin::onLoad)
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader)
        return id
    }

    fun loadPlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        loadPlugin(path)
    }

    fun unloadPlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        val classLoader = DexClassLoader(
            path.path,
            appContext.cacheDir.resolve("plugin/optimizedDirectory").path,
            null,
            this::class.java.classLoader
        )
        webBookDataSourceManager.unLoadWebDataSourcesFromPackages(classLoader)
    }

    fun deletePlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        unloadPlugin(id)
        path.delete()
        enabledPluginsUserData.update {
            it.toMutableList().apply { remove(id) }
        }
        _allPluginInfo.removeIf { it.id == id }
    }
}