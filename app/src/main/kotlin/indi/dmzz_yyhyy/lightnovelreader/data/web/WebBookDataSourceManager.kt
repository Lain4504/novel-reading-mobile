package indi.dmzz_yyhyy.lightnovelreader.data.web

import dalvik.system.DexClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.AnnotationScanner
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebBookDataSourceManager @Inject constructor (
    val userDataRepository: UserDataRepository,
) {
    private val _webDataSourceItems = mutableListOf<WebDataSourceItem>()
    val webDataSourceItems: List<WebDataSourceItem> = _webDataSourceItems
    private val webBookDataSources = mutableListOf<WebBookDataSource>()

    fun loadWebDataSourcesFromClassLoader(classLoader: DexClassLoader) {
        AnnotationScanner.findAnnotatedClasses(classLoader, WebDataSource::class.java)
            .forEach {
                val instance =
                    try { it.getDeclaredField("INSTANCE").get(null) }
                    catch (_: NoSuchFieldException) { null }
                        ?: it.getDeclaredConstructor().newInstance()
                if (instance is WebBookDataSource) loadWebDataSourceClass(instance)
            }
    }

    fun loadWebDataSourceClass(instance: WebBookDataSource) {
        webBookDataSources.add(instance)
        val info = instance.javaClass.getAnnotationsByType(WebDataSource::class.java)
        _webDataSourceItems.add(
            WebDataSourceItem(
                instance.id,
                info.first().name,
                info.first().provider,
            )
        )
    }

    fun unLoadWebDataSourcesFromPackages(classLoader: DexClassLoader) {
        AnnotationScanner.findAnnotatedClasses(classLoader, WebDataSource::class.java)
            .forEach {
                val instance =
                    try { it.getDeclaredField("INSTANCE").get(null) }
                    catch (_: NoSuchFieldException) { null }
                        ?: it.getDeclaredConstructor().newInstance()
                if (instance is WebBookDataSource) {
                    webBookDataSources.remove(instance)
                    val info = it.getAnnotationsByType(WebDataSource::class.java)
                    _webDataSourceItems.remove(
                        WebDataSourceItem(
                            instance.id,
                            info.first().name,
                            info.first().provider,
                        )
                    )
                }
            }
    }

    fun getWebDataSource(): WebBookDataSource {
        val webDataSourcesId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).getOrDefault("wenku8".hashCode())
        return webBookDataSources.find { it.id == webDataSourcesId } ?: EmptyWebDataSource
    }
}