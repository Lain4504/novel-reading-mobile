package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavGraphBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import io.nightfish.lightnovelreader.api.PluginContext
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import java.io.File
import java.util.zip.ZipFile
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
    private val defaultWebDataSources = listOf(Wenku8Api)
    val pluginsDir = appContext.dataDir.resolve("plugin")
    fun getPluginDir(name: String) = pluginsDir.resolve(name)
    fun getPluginFile(pluginDir: File) = pluginDir.resolve("plugin")
    fun getPluginDataDir(pluginDir: File) = pluginDir.resolve("data")
    fun getPluginAssetDir(pluginDir: File) = pluginDir.resolve("asset")
    fun getPluginLibsDir(pluginDir: File) = pluginDir.resolve("libs")

    fun loadAllPlugins() {
        errorPluginsUserData.get()?.forEach { path ->
            File(path).also {
                it.delete()
                if (it.parentFile?.parentFile == pluginsDir) {
                    it.parentFile!!.deleteRecursively()
                }
            }
            errorPluginsUserData.update {
                it.toMutableList().apply {
                    remove(path)
                }
            }
        }
        defaultWebDataSources.forEach(webBookDataSourceManager::loadWebDataSourceClass)
        pluginsDir
            .also(File::mkdir)
            .listFiles()
            ?.filter { it.isDirectory }
            ?.forEach {
                loadPlugin(getPluginFile(it))
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

    private fun getPluginInstance(clazz: Class<*>, pluginContext: PluginContext): LightNovelReaderPlugin? {
        if (!LightNovelReaderPlugin::class.java.isAssignableFrom(clazz)) return null
        return pluginInjector.providePlugin(clazz, pluginContext)
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
        val packageManager = appContext.packageManager
        val pluginDir = path.parentFile ?: return null
        val packageInfo = packageManager.getPackageArchiveInfo(path.path, PackageManager.GET_PERMISSIONS)?.also { packageInfo ->
            val dir = getPluginAssetDir(pluginDir)
                .also {
                    if (it.exists()) it.deleteRecursively()
                    it.mkdirs()
                }
            val zipFile = ZipFile(path)
            val entries = zipFile.entries()

            for (entry in entries) {
                if (entry.name.startsWith("assets")) {
                    zipFile.getInputStream(entry).buffered().use { inputStream ->
                        val outputFile = dir.resolve(entry.name.replaceFirst("assets/", ""))
                            .also { it.parentFile?.mkdirs() }
                        outputFile.outputStream().buffered().use {
                            inputStream.copyTo(it)
                        }
                    }
                }
            }
            zipFile.close()
            extractLibFromApk(path, getPluginLibsDir(pluginDir).also { it.mkdirs() })
        }
        val classLoader = DexClassLoader(
            path.absolutePath,
            null,
            getPluginLibsDir(pluginDir).absolutePath,
            this.javaClass.classLoader
        )
        val scanPackage = packageInfo?.packageName ?: ""
        return loadPlugin(
            classLoader,
            PluginContext(
                dataDir = getPluginDataDir(pluginDir),
                pluginFile = getPluginFile(pluginDir),
                assetDir = getPluginAssetDir(pluginDir)
            ),
            scanPackage,
            forceLoad
        ).also {
            if (it != null) {
                pluginPathMap[it] = path
            }
        }
    }

    fun loadPlugin(classLoader: DexClassLoader, pluginContext: PluginContext, scanPackage: String = "", forceLoad: Boolean = false): String? {
        var id: String? = null
        AnnotationScanner.findAnnotatedClasses(classLoader, Plugin::class.java, scanPackage)
            .filter {
                id = getPluginId(it)
                id != null
            }
            .map { getPluginInstance(it, pluginContext) }
            .filter { it is LightNovelReaderPlugin }
            .map { it as LightNovelReaderPlugin }
            .firstOrNull()
            ?.let {
                loadPlugin(it, forceLoad = forceLoad)
            }
        webBookDataSourceManager.loadWebDataSourcesFromClassLoader(classLoader, pluginInjector, scanPackage)
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
        if (path.parentFile?.parentFile == pluginsDir) {
            path.parentFile!!.deleteRecursively()
        }
        enabledPluginsUserData.update {
            it.toMutableList().apply { remove(id) }
        }
        _allPluginInfo.removeIf { it.id == id }
    }



    private fun extractLibFromApk(apk: File, targetDir: File) {
        try {
            val tempDir = targetDir.resolve("temp").also { it.mkdir() }
            val packageInfo = appContext.packageManager.getPackageArchiveInfo(apk.path, 0)
            packageInfo?.applicationInfo?.let { appInfo ->
                val sourceDir = apk.path
                val zipFile = ZipFile(sourceDir)
                val entries = zipFile.entries()

                for (entry in entries) {
                    if (entry.name.startsWith("lib/") && !entry.isDirectory && !entry.name.endsWith("libandroidx.graphics.path.so")) {
                        zipFile.getInputStream(entry).buffered().use { inputStream ->
                            val outputFile = tempDir.resolve(entry.name.replaceFirst("lib/", ""))
                                .also { file ->
                                    file.parentFile?.mkdirs()
                                }
                                .also { it.createNewFile() }
                            outputFile.outputStream().buffered().use {
                                inputStream.copyTo(it)
                            }
                        }
                    }
                }
                zipFile.close()
            }
            val abiList = Build.SUPPORTED_ABIS
            for (abi in abiList.reversed()) {
                val abiDir = tempDir.resolve(abi)
                if (!abiDir.exists()) continue
                abiDir.listFiles()?.forEach { file ->
                    val outputFile = targetDir.resolve(file.name)
                        .also(File::createNewFile)
                    outputFile.outputStream().buffered().use {
                        file.inputStream().buffered().copyTo(it)
                    }
                }
            }
            tempDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    fun PluginContent(pluginId: String, paddingValues: PaddingValues) {
        pluginMap[pluginId]?.PageContent(paddingValues)
    }

    fun NavGraphBuilder.onBuildNavHost() {
        pluginMap.values.forEach {
            with(it) {
                onBuildNavHost()
            }
        }
    }
}