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
import indi.dmzz_yyhyy.lightnovelreader.utils.getApkSignatures
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    private val computeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun loadAllPlugins() {
        errorPluginsUserData.get()?.forEach { path ->
            File(path).delete()
            errorPluginsUserData.update { it.toMutableList().apply { remove(path) } }
        }

        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        defaultPlugins.forEach { getPluginInstance(it)?.let { p -> loadPlugin(p, forceLoad = true) } }

        val pluginDir = appContext.dataDir.resolve("plugin").apply { mkdirs() }
        pluginDir.listFiles()?.forEach {
            loadPlugin(it)
        }
    }

    private fun getPluginId(clazz: Class<*>): String? {
        val annotation = clazz.getAnnotation(Plugin::class.java) ?: return null
        val id = clazz.`package`?.name ?: return null
        val existingSignatures = _allPluginInfo.firstOrNull { it.id == id }?.signatures
        val info = PluginInfo(
            isUpdatable = false,
            id = id,
            name = annotation.name,
            version = annotation.version,
            versionName = annotation.versionName,
            author = annotation.author,
            description = annotation.description,
            updateUrl = annotation.updateUrl,
            signatures = existingSignatures
        )
        _allPluginInfo.removeAll { it.id == info.id }
        _allPluginInfo.add(info)
        return id
    }

    private fun getPluginInstance(clazz: Class<*>): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz)
    }

    fun loadPlugin(plugin: LightNovelReaderPlugin, forceLoad: Boolean = false) {
        val id = getPluginId(plugin.javaClass) ?: return
        if (!enabledPluginsUserData.getOrDefault(emptyList()).contains(id) && !forceLoad) return
        plugin.onLoad()
        pluginMap[id] = plugin
    }

    fun loadPlugin(path: File, forceLoad: Boolean = false): String? {
        path.setReadOnly()
        val optimizedDir = appContext.cacheDir.resolve("plugin/optimizedDirectory").apply { mkdirs() }
        val classLoader = DexClassLoader(
            path.path,
            optimizedDir.path,
            null,
            this.javaClass.classLoader
        )

        val packageManager = appContext.packageManager
        val packageInfo = packageManager.getPackageArchiveInfo(path.path, PackageManager.GET_PERMISSIONS)
        val scanPackage = packageInfo?.packageName ?: ""

        val id = loadPlugin(classLoader, scanPackage, forceLoad)
        if (id != null) {
            pluginPathMap[id] = path
            computeScope.launch {
                println("TRIGGER -> getApkSig for $id")
                val sig = try { getApkSignatures(path) } catch (_: Throwable) { null }
                val info = _allPluginInfo.first { it.id == id }
                _allPluginInfo.removeAll { it.id == id }.also {
                    _allPluginInfo.add(info.copy(signatures = sig))
                }
            }
        }
        return id
    }

    fun loadPlugin(classLoader: DexClassLoader, scanPackage: String = "", forceLoad: Boolean = false): String? {
        var id: String? = null
        AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java, scanPackage)
            .filter {
                val tmpId = getPluginId(it)
                id = tmpId
                tmpId != null
            }.firstNotNullOfOrNull(::getPluginInstance)
            ?.let { inst ->
                val pid = id ?: return@let
                if (forceLoad || enabledPluginsUserData.getOrDefault(emptyList()).contains(pid)) {
                    inst.onLoad()
                    pluginMap[pid] = inst
                }
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

    fun upgradePlugin(id: String, newFile: File): Boolean {
        val oldFile = pluginPathMap[id] ?: return false
        unloadPlugin(id)

        val dir = oldFile.parentFile ?: return false
        val tmp = File(dir, "${oldFile.name}.tmp")
        val bak = File(dir, "${oldFile.name}.bak")

        try {
            if (tmp.exists()) tmp.delete()
            newFile.copyTo(tmp, overwrite = true)
            newFile.delete()
            tmp.setReadOnly()
        } catch (_: Throwable) {
            tmp.delete()
            loadPlugin(oldFile, forceLoad = true)
            return false
        }

        try {
            if (bak.exists()) bak.delete()
            if (!oldFile.renameTo(bak)) {
                tmp.delete()
                loadPlugin(oldFile, forceLoad = true)
                return false
            }
        } catch (_: Throwable) {
            tmp.delete()
            loadPlugin(oldFile, forceLoad = true)
            return false
        }

        if (!tmp.renameTo(oldFile)) {
            bak.renameTo(oldFile)
            tmp.delete()
            loadPlugin(oldFile, forceLoad = true)
            return false
        }

        _allPluginInfo.removeAll { it.id == id }
        val loadedId = try { loadPlugin(oldFile, forceLoad = true) } catch (_: Throwable) { null }

        return if (loadedId == id) {
            try { bak.delete() } catch (_: Throwable) {}
            pluginPathMap[id] = oldFile
            true
        } else {
            try { oldFile.delete() } catch (_: Throwable) {}
            try { bak.renameTo(oldFile) } catch (_: Throwable) {}
            try { tmp.delete() } catch (_: Throwable) {}
            loadPlugin(oldFile, forceLoad = true)
            false
        }
    }

    fun deletePlugin(id: String) {
        val path = pluginPathMap[id] ?: return
        unloadPlugin(id)
        path.delete()
        enabledPluginsUserData.update { it.toMutableList().apply { remove(id) } }
        _allPluginInfo.removeAll { it.id == id }
        pluginPathMap.remove(id)
        pluginMap.remove(id)
        pluginClassLoaderMap.remove(id)
    }

    @Composable
    fun PluginContent(pluginId: String, paddingValues: PaddingValues) {
        pluginMap[pluginId]?.PageContent(paddingValues)
    }

    fun getPluginInfo(id: String): PluginInfo? =
        _allPluginInfo.firstOrNull { it.id == id }

    fun getPluginFile(id: String): File? =
        pluginPathMap[id]
}
