package com.miraimagiclab.novelreadingapp.data.explore
import com.miraimagiclab.novelreadingapp.data.text.TextProcessingRepository
import com.miraimagiclab.novelreadingapp.data.web.WebBookDataSourceProvider
import io.lain4504.novelreadingapp.api.book.BookInformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        val cacheKey = searchType + keyword
        searchResultCacheMap[cacheKey]?.let { cachedResult ->
            val cachedFlow = kotlinx.coroutines.flow.flow {
                emit(cachedResult)
                emit(listOf(BookInformation.empty()))
            }
            return cachedFlow.map { list ->
                list.map { processingRepository.processBookInformation { it } }
            }
        }
        val flow = webBookDataSourceProvider.value.search(searchType, keyword)
        coroutineScope.launch {
            var latestResult: List<BookInformation> = emptyList()
            flow.collect { list ->
                if (list.isNotEmpty() && list.last().isEmpty()) {
                    searchResultCacheMap[cacheKey] = latestResult
                } else {
                    latestResult = list
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