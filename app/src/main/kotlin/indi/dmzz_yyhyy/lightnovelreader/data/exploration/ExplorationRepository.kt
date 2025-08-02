package indi.dmzz_yyhyy.lightnovelreader.data.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
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
class ExplorationRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource,
    private val processingRepository: TextProcessingRepository
) {
    private val searchResultCacheMap = mutableMapOf<String, List<BookInformation>>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val searchTypeIdList = webBookDataSource.searchTypeIdList
    val searchTypeMap = processingRepository.processSearchTypeNameMap { webBookDataSource.searchTypeMap }
    val searchTipMap = processingRepository.processSearchTipMap { webBookDataSource.searchTipMap }
    val explorationPageIdList = webBookDataSource.explorationPageIdList
    val explorationPageDataSourceMap = webBookDataSource.explorationPageDataSourceMap
    val explorationExpandedPageDataSourceMap = webBookDataSource.explorationExpandedPageDataSourceMap

    fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        searchResultCacheMap[searchType + keyword]?.let { searchResult ->
            val flow = MutableStateFlow(emptyList<BookInformation>())
            flow.update { searchResult }
            return flow
        }
        val flow = webBookDataSource.search(searchType, keyword)
        coroutineScope.launch {
            flow.collect {
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    searchResultCacheMap[searchType + keyword] = it
                }
            }
        }

        return flow.map {
            it.map {
                processingRepository.processBookInformation { it }
            }
        }
    }
    fun stopAllSearch() = webBookDataSource.stopAllSearch()
}