package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object EmptyWebDataSource: WebBookDataSource {
    override val id: Int = -1
    override suspend fun isOffLine(): Boolean = true

    override val offLine: Boolean = true
    override val isOffLineFlow: Flow<Boolean> = flow { true }
    override val explorationPageIdList: List<String> = emptyList()
    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> = emptyMap()
    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> = emptyMap()
    override val searchTypeMap: Map<String, String> = emptyMap()
    override val searchTipMap: Map<String, String> = emptyMap()
    override val searchTypeIdList: List<String> = emptyList()
    override suspend fun getBookInformation(id: Int): BookInformation = BookInformation.empty()

    override suspend fun getBookVolumes(id: Int): BookVolumes = BookVolumes.empty(-1)

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent = ChapterContent.empty()

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow { emptyList<BookInformation>() }

    override fun stopAllSearch() {}
}