package io.nightfish.lightnovelreader.plugin.js

import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class LazyLoadWebDataSource(
    dataSourceId: Int,
    val webBookDataSourceProvider: () -> WebBookDataSource
): WebBookDataSource {
    var bookDataSource: WebBookDataSource = EmptyWebDataSource
    override val id: Int = dataSourceId
    override suspend fun isOffLine() = bookDataSource.isOffLine()
    override val offLine = bookDataSource.offLine
    override val isOffLineFlow = bookDataSource.isOffLineFlow
    override val explorePageIdList = bookDataSource.explorePageIdList
    override val explorePageDataSourceMap = bookDataSource.explorePageDataSourceMap
    override val exploreExpandedPageDataSourceMap = bookDataSource.exploreExpandedPageDataSourceMap
    override val searchTypeMap = bookDataSource.searchTypeMap
    override val searchTipMap = bookDataSource.searchTipMap
    override val searchTypeIdList = bookDataSource.searchTypeIdList
    override suspend fun getBookInformation(id: Int) = bookDataSource.getBookInformation(id)
    override suspend fun getBookVolumes(id: Int) = bookDataSource.getBookVolumes(id)
    override suspend fun getChapterContent(chapterId: Int, bookId: Int) = bookDataSource.getChapterContent(chapterId, bookId)
    override fun search(searchType: String, keyword: String) = bookDataSource.search(searchType, keyword)
    override fun stopAllSearch() = bookDataSource.stopAllSearch()

    override fun onLoad() {
        bookDataSource = webBookDataSourceProvider.invoke()
    }
}