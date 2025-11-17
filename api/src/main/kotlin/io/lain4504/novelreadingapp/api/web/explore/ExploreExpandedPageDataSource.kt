package io.lain4504.novelreadingapp.api.web.explore

import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.web.explore.filter.Filter
import kotlinx.coroutines.flow.Flow

interface ExploreExpandedPageDataSource {
    fun getTitle(): String
    fun getFilters(): List<Filter>
    fun getResultFlow(): Flow<List<BookInformation>>
    fun refresh()
    suspend fun loadMore()
    fun hasMore(): Boolean
}