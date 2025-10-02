package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.bilinovel.BiliNovel
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginManager @Inject constructor(
    @field:ApplicationContext private val appContext: Context,
    private val webBookDataSourceManager: WebBookDataSourceManager,
    private val pluginInjector: PluginInjector,
    userDataRepository: UserDataRepository
) {
    private val _allPluginInfo = mutableStateListOf<PluginInfo>()
    private val pluginPathMap = mutableMapOf<String, File>()
    private val pluginMap = mutableMapOf<String, LightNovelReaderPlugin>()
    private val pluginClassLoaderMap = mutableMapOf<String, DexClassLoader>()
    val allPluginInfo: SnapshotStateList<PluginInfo> = _allPluginInfo
    private val enabledPluginsUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.EnabledPlugins.path)
    private val errorPluginsUserData = userDataRepository.stringListUserData(UserDataPath.Plugin.ErrorPlugins.path)
    private val defaultWebDataSources = listOf(Wenku8Api, ZaiComic, BiliNovel)
    private val defaultPlugins = listOf<Class<*>>()

    fun loadAllPlugins() {
        errorPluginsUserData.get()?.forEach { path ->
            File(path).delete()
            errorPluginsUserData.update {
                it.toMutableList().apply {
                    remove(path)
                }
            }
        }
        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        defaultPlugins.forEach { getPluginInstance(it)?.let { plugin -> loadPlugin(plugin, forceLoad = true) } }
        appContext.dataDir.resolve("plugin")
            .also(File::mkdir)
            .listFiles()
            ?.forEach {
                loadPlugin(it)
            }
    }

    private fun getPluginId(plugin: Class<*>): String? {
        var id: String?
        val annotation = plugin.getAnnotation(Plugin::class.java)
        if (annotation != null) {
            id = plugin.`package`?.name ?: return null
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

    private fun getPluginInstance(clazz: Class<*>): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin, forceLoad: Boolean = false) {
        val id = getPluginId(plugin.javaClass) ?: return
        if (!enabledPluginsUserData.getOrDefault(emptyList()).contains(id) && !forceLoad) return
        plugin.apply {
            onLoad()
        }
        pluginMap[id] = plugin
    }

    fun loadPlugin(path: File, forceLoad: Boolean = false): String? {
        path.setReadOnly()
        val classLoader = DexClassLoader(
            path.path,
            appContext.cacheDir.resolve("plugin/optimizedDirectory").path,
            null,
            this.javaClass.classLoader
        )
        val packageManager = appContext.packageManager
        val packageInfo = packageManager.getPackageArchiveInfo(path.path, PackageManager.GET_PERMISSIONS)
        val scanPackage = packageInfo?.packageName ?: ""
        return loadPlugin(
            classLoader,
            scanPackage,
            forceLoad
        ).also {
            if (it != null) {
                pluginPathMap[it] = path
            }
        }
    }

    fun loadPlugin(classLoader: DexClassLoader, scanPackage: String = "", forceLoad: Boolean = false): String? {
        var id: String? = null
        AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java, scanPackage)
            .filter {
                id = getPluginId(it)
                id != null
            }
            .map(::getPluginInstance)
            .filter { it is LightNovelReaderPlugin }
            .map { it as LightNovelReaderPlugin }
            .firstOrNull()
            ?.let {
                loadPlugin(it, forceLoad = forceLoad)
            }
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader, scanPackage)
        if (id != null) {
            pluginClassLoaderMap[id] = classLoader
        }
        return id
    }

    fun loadPlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        loadPlugin(path)
    }

    fun unloadPlugin(id: String) {
        pluginMap[id]?.onUnload()
        pluginClassLoaderMap[id]?.let { webBookDataSourceManager.unloadWebDataSourcesFromClassLoader(it) }
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

    @Composable
    fun PluginContent(pluginId: String, paddingValues: PaddingValues) {
        pluginMap[pluginId]?.PageContent(paddingValues)
    }
}