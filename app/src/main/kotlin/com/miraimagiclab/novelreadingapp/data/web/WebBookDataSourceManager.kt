package com.miraimagiclab.novelreadingapp.data.web

import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import io.lain4504.novelreadingapp.api.web.WebBookDataSource
import io.lain4504.novelreadingapp.api.web.WebBookDataSourceManagerApi
import io.lain4504.novelreadingapp.api.web.WebDataSource
import io.lain4504.novelreadingapp.api.web.WebDataSourceItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.JvmSuppressWildcards

@Singleton
class WebBookDataSourceManager @Inject constructor(
    val userDataRepository: UserDataRepository,
    dataSources: Set<@JvmSuppressWildcards WebBookDataSource>
) : WebBookDataSourceManagerApi {
    private val _webDataSourceItems = mutableListOf<WebDataSourceItem>()
    val webDataSourceItems: List<WebDataSourceItem> = _webDataSourceItems

    private val mutableWebDataSourceProvider = MutableWebDataSourceProvider()
    private val webBookDataSources = mutableListOf<WebBookDataSource>()

    init {
        dataSources.forEach { dataSource ->
            loadWebDataSourceClass(dataSource)
        }
    }

    override fun registerWebDataSource(webBookDataSource: WebBookDataSource, webDataSourceItem: WebDataSourceItem) {
        if (_webDataSourceItems.any { it.id == webDataSourceItem.id }) return
        _webDataSourceItems.add(webDataSourceItem)
        webBookDataSources.add(webBookDataSource)
        onWebDataSourceListChange()
    }

    override fun unregisterWebDataSource(webDataSourceId: Int) {
        _webDataSourceItems.removeAll { it.id == webDataSourceId }
        webBookDataSources.removeAll { it.id == webDataSourceId }
        onWebDataSourceListChange()
    }

    override fun getWebDataSource(): WebBookDataSource = mutableWebDataSourceProvider.value

    private fun loadWebDataSourceClass(instance: WebBookDataSource): WebDataSourceItem {
        val info = instance.javaClass.getAnnotationsByType(WebDataSource::class.java)
        val item = WebDataSourceItem(
            instance.id,
            info.first().name,
            info.first().provider,
        )
        registerWebDataSource(instance, item)
        return item
    }

    fun getWebDataSourceProvider(): WebBookDataSourceProvider {
        return mutableWebDataSourceProvider
    }

    fun onWebDataSourceListChange() {
        // Default to backend-api if available, otherwise use saved preference
        val backendDataSourceId = "backend-api".hashCode()
        val savedDataSourceId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).getOrDefault(backendDataSourceId)
        
        mutableWebDataSourceProvider.value =
            webBookDataSources
                .find { it.id == savedDataSourceId }
                ?: webBookDataSources.find { it.id == backendDataSourceId }
                ?.also {
                    it.onLoad()
                } ?: EmptyWebDataSource
    }
}