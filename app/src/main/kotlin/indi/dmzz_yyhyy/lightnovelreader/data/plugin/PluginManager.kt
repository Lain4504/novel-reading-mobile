package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
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
    private val enabledPlugins = enabledPluginsUserData.getOrDefault(emptyList())

    fun loadAllPlugins() {
        appContext.assets.list("")
            ?.filter { it.endsWith(".dex") }
            ?.forEach { path ->
                appContext.assets.open(path).use { pluginInputStream ->
                    appContext.cacheDir.resolve("plugin").let { dir ->
                        if (!dir.exists()) dir.mkdir()
                        appContext.cacheDir.resolve("plugin/$path").let { file ->
                            file.createNewFile()
                            file.outputStream().use { outputStream ->
                                pluginInputStream.copyTo(outputStream)
                            }
                        }
                        loadPlugin(appContext.cacheDir.resolve("plugin/$path"), ignorePluginInfo = true)
                    }
                }

            }
        appContext.dataDir.resolve("plugin")
            .also(File::mkdir)
            .listFiles { it.name.endsWith(".dex") }
            ?.forEach(::loadPlugin)
    }

    fun loadPlugin(path: File, ignorePluginInfo: Boolean = false, forceLoad: Boolean = false): String? {
        val classLoader = DexClassLoader(
            path.path,
            appContext.cacheDir.resolve("plugin/optimizedDirectory").path,
            null,
            this::class.java.classLoader
        )
        var loadable = false
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
                val annotation = it.javaClass.getAnnotation(Plugin::class.java)
                if (annotation != null) {
                    id = it.javaClass.`package`?.name ?: return null
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
                    if (!enabledPlugins.contains(id) && !forceLoad) return id
                    loadable = true
                }
            }
            ?.let(LightNovelReaderPlugin::onLoad)
        if (loadable || ignorePluginInfo)
            webBookDataSourceManager.loadWebDataSourcesFromPackages(classLoader)
        if (id != null)
            pluginPathMap[id] = path
        return id
    }

    fun loadPlugin(id: String, ignorePluginInfo: Boolean = false, forceLoad: Boolean = false) {
        val path = pluginPathMap[id] ?: return
        loadPlugin(path, ignorePluginInfo, forceLoad)
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