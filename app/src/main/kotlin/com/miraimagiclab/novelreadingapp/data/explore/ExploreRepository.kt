package com.miraimagiclab.novelreadingapp.data.explore

import com.miraimagiclab.novelreadingapp.data.text.TextProcessingRepository
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import io.lain4504.novelreadingapp.api.book.BookInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
    private val processingRepository: TextProcessingRepository
) {
    private val searchResultCacheMap = mutableMapOf<String, List<BookInformation>>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val searchTypeIdList = webBookDataSourceProvider.value.searchTypeIdList
    val searchTypeMap = processingRepository.processSearchTypeNameMap { webBookDataSourceProvider.value.searchTypeMap }
    val searchTipMap = processingRepository.processSearchTipMap { webBookDataSourceProvider.value.searchTipMap }
    val explorePageIdList = webBookDataSourceProvider.value.explorePageIdList
    val explorePageDataSourceMap = webBookDataSourceProvider.value.explorePageDataSourceMap
    val exploreExpandedPageDataSourceMap = webBookDataSourceProvider.value.exploreExpandedPageDataSourceMap

    fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        searchResultCacheMap[searchType + keyword]?.let { searchResult ->
            val flow = MutableStateFlow(emptyList<BookInformation>())
            flow.update { searchResult }
            return flow
        }
        val flow = webBookDataSourceProvider.value.search(searchType, keyword)
        coroutineScope.launch {
            flow.collect {
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    searchResultCacheMap[searchType + keyword] = it
                }
            }
        }

        return flow.map { list ->
            list.map {
                processingRepository.processBookInformation { it }
            }
        }
    }

    fun stopAllSearch() = webBookDataSourceProvider.value.stopAllSearch()
}