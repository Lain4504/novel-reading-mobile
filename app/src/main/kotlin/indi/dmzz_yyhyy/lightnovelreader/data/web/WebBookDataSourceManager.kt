package indi.dmzz_yyhyy.lightnovelreader.data.web

import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebBookDataSourceManager @Inject constructor (
    val userDataRepository: UserDataRepository,
): io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi {
    private val _webDataSourceItems = mutableListOf<WebDataSourceItem>()
    private val webDataSourceItemListMap = mutableMapOf<DexClassLoader, List<WebDataSourceItem>>()
    val webDataSourceItems: List<WebDataSourceItem> = _webDataSourceItems

    private val mutableWebDataSourceProvider = MutableWebDataSourceProvider()
    private val webBookDataSources = mutableListOf<WebBookDataSource>()

    override fun registerWebDataSource(webBookDataSource: WebBookDataSource, webDataSourceItem: WebDataSourceItem) {
        _webDataSourceItems.add(webDataSourceItem)
        webBookDataSources.add(webBookDataSource)
        onWebDataSourceListChange()
    }

    override fun unregisterWebDataSource(webDataSourceId: Int) {
        _webDataSourceItems.removeAll { it.id == webDataSourceId }
        webBookDataSources.removeAll { it.id == webDataSourceId }
        onWebDataSourceListChange()
    }

    fun loadWebDataSourcesFromClassLoader(classLoader: DexClassLoader, scanPackage: String = "") {
        val items = mutableListOf<WebDataSourceItem>()
        AnnotationScanner.findAnnotatedClasses(classLoader, WebDataSource::class.java, scanPackage)
            .forEach {
                val instance =
                    try { it.getDeclaredField("INSTANCE").get(null) }
                    catch (_: NoSuchFieldException) { null }
                        ?: it.getDeclaredConstructor().newInstance()
                if (instance is WebBookDataSource) items.add(loadWebDataSourceClass(instance))
            }
        webDataSourceItemListMap[classLoader] = items
    }

    fun loadWebDataSourceClass(instance: WebBookDataSource): WebDataSourceItem {
        val info = instance.javaClass.getAnnotationsByType(WebDataSource::class.java)
        val item = WebDataSourceItem(
            instance.id,
            info.first().name,
            info.first().provider,
        )
        registerWebDataSource(instance, item)
        return item
    }

    fun unloadWebDataSourcesFromClassLoader(dexClassLoader: DexClassLoader) {
        webDataSourceItemListMap[dexClassLoader]?.let { _webDataSourceItems.removeAll(it) }
    }

    fun getWebDataSourceProvider(): WebBookDataSourceProvider {
        return mutableWebDataSourceProvider
    }

    fun onWebDataSourceListChange() {
        val webDataSourcesId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).getOrDefault("wenku8".hashCode())
        mutableWebDataSourceProvider.value = webBookDataSources.find { it.id == webDataSourcesId } ?: EmptyWebDataSource
    }
}