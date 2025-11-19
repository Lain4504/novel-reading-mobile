package com.miraimagiclab.novelreadingapp.data.graphql

import android.net.Uri
import androidx.navigation.NavController
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.miraimagiclab.novelreadingapp.graphql.CategoryRandomNovelsQuery
import com.miraimagiclab.novelreadingapp.graphql.CompletedNovelsQuery
import com.miraimagiclab.novelreadingapp.graphql.GetChapterQuery
import com.miraimagiclab.novelreadingapp.graphql.GetChaptersQuery
import com.miraimagiclab.novelreadingapp.graphql.GetNovelQuery
import com.miraimagiclab.novelreadingapp.graphql.GetVolumesQuery
import com.miraimagiclab.novelreadingapp.graphql.LatestNovelsQuery
import com.miraimagiclab.novelreadingapp.graphql.RecentNovelsQuery
import com.miraimagiclab.novelreadingapp.graphql.SearchNovelsQuery
import com.miraimagiclab.novelreadingapp.graphql.type.CategoryEnum
import com.miraimagiclab.novelreadingapp.utils.md.HtmlToMdUtil
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.book.BookVolumes
import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.book.ChapterInformation
import io.lain4504.novelreadingapp.api.book.MutableBookInformation
import io.lain4504.novelreadingapp.api.book.MutableChapterContent
import io.lain4504.novelreadingapp.api.book.Volume
import io.lain4504.novelreadingapp.api.book.WorldCount
import io.lain4504.novelreadingapp.api.content.builder.ContentBuilder
import io.lain4504.novelreadingapp.api.content.builder.simpleText
import io.lain4504.novelreadingapp.api.explore.ExploreBooksRow
import io.lain4504.novelreadingapp.api.explore.ExploreDisplayBook
import io.lain4504.novelreadingapp.api.explore.ExplorePage
import io.lain4504.novelreadingapp.api.web.WebBookDataSource
import io.lain4504.novelreadingapp.api.web.WebDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExploreExpandedPageDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@WebDataSource(
    name = "GraphQL Backend",
    provider = "Apollo Kotlin"
)
@Singleton
class GraphQLBookService @Inject constructor(
    private val apolloClient: ApolloClient
) : WebBookDataSource {

    private val completionMarker = BookInformation.empty()
    private val searchResolvers: Map<String, suspend (String) -> List<BookInformation>> = mapOf(
        SEARCH_TYPE_TITLE to ::searchByTitle
    )
    private val explorePages: Map<String, ExplorePageDataSource> = linkedMapOf(
        DISCOVER_PAGE_ID to createDiscoverExplorePage(),
        TRENDING_PAGE_ID to createTrendingExplorePage(),
        GENRES_PAGE_ID to createGenresExplorePage()
    )
    
    override val id: Int = "backend-api".hashCode()
    
    private val _offLine = MutableStateFlow(false)
    override val offLine: Boolean
        get() = _offLine.value
    
    override val isOffLineFlow: Flow<Boolean> = _offLine
    
    override val explorePageIdList: List<String> = explorePages.keys.toList()
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> = explorePages
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> = emptyMap()
    override val searchTypeMap: Map<String, String> = mapOf(
        SEARCH_TYPE_TITLE to "Title"
    )
    override val searchTipMap: Map<String, String> = mapOf(
        SEARCH_TYPE_TITLE to "Search by novel title"
    )
    override val searchTypeIdList: List<String> = listOf(SEARCH_TYPE_TITLE)
    
    override fun onLoad() {
        // Initialize if needed
    }
    
    override suspend fun isOffLine(): Boolean {
        // TODO: Implement network check
        return false
    }
    
    override suspend fun getBookInformation(id: String): BookInformation {
        return try {
            val response = apolloClient.query(GetNovelQuery(id = id)).execute()
            
            response.data?.novel?.let { novel ->
                convertToBookInformation(novel)
            } ?: BookInformation.empty(id)
        } catch (e: Exception) {
            e.printStackTrace()
            BookInformation.empty(id)
        }
    }

    private fun createTrendingExplorePage(): ExplorePageDataSource {
        return object : ExplorePageDataSource {
            override val title: String = TRENDING_PAGE_TITLE
            override fun getExplorePage(): ExplorePage {
                val rowsFlow = flow {
                    val rows = mutableListOf<ExploreBooksRow>()
                    emitRowIfNotEmpty(rows, "Hot this week", fetchRecentNovels(page = 0, size = DEFAULT_ROW_LIMIT * 3, limit = DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Readers' picks", fetchRecentNovels(page = 1, size = DEFAULT_ROW_LIMIT * 3, limit = DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Freshly completed", fetchCompletedNovels(DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Action & Adventure", fetchCategoryRandomNovels(CategoryEnum.ADVENTURE, DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Urban legends", fetchCategoryRandomNovels(CategoryEnum.URBAN, DEFAULT_ROW_LIMIT))
                }
                return ExplorePage(title = title, rows = rowsFlow)
            }
        }
    }

    private fun createGenresExplorePage(): ExplorePageDataSource {
        val genreConfigs = listOf(
            "Fantasy realms" to CategoryEnum.FANTASY,
            "Romantic tales" to CategoryEnum.ROMANCE,
            "Slice of life" to CategoryEnum.SLICE_OF_LIFE,
            "School life" to CategoryEnum.SCHOOL_LIFE,
            "Drama & feels" to CategoryEnum.DRAMA,
            "Comedy relief" to CategoryEnum.COMEDY
        )
        return object : ExplorePageDataSource {
            override val title: String = GENRES_PAGE_TITLE
            override fun getExplorePage(): ExplorePage {
                val rowsFlow = flow {
                    val rows = mutableListOf<ExploreBooksRow>()
                    genreConfigs.forEach { (label, category) ->
                        emitRowIfNotEmpty(rows, label, fetchCategoryRandomNovels(category, DEFAULT_ROW_LIMIT))
                    }
                }
                return ExplorePage(title = title, rows = rowsFlow)
            }
        }
    }
    
    override suspend fun getBookVolumes(id: String): BookVolumes {
        return try {
            val response = apolloClient.query(GetVolumesQuery(novelId = id)).execute()
            
            val volumes = response.data?.volumes?.mapNotNull { volume ->
                volume?.let { convertToVolume(it) }
            } ?: emptyList()
            
            BookVolumes(id, volumes)
        } catch (e: Exception) {
            e.printStackTrace()
            BookVolumes.empty(id)
        }
    }
    
    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        return try {
            val response = apolloClient.query(GetChapterQuery(id = chapterId)).execute()
            
            response.data?.chapter?.let { chapter ->
                convertToChapterContent(chapter)
            } ?: ChapterContent.empty(chapterId)
        } catch (e: Exception) {
            e.printStackTrace()
            ChapterContent.empty(chapterId)
        }
    }
    
    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isEmpty()) {
            emit(emptyList())
            emit(listOf(completionMarker))
            return@flow
        }
        val resolver = searchResolvers[searchType] ?: searchResolvers.getValue(SEARCH_TYPE_TITLE)
        val novels = try {
            resolver(normalizedKeyword).also {
                _offLine.value = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _offLine.value = true
            emptyList()
        }
        emit(novels)
        emit(listOf(completionMarker))
    }

    private fun createDiscoverExplorePage(): ExplorePageDataSource {
        return object : ExplorePageDataSource {
            override val title: String = DISCOVER_PAGE_TITLE
            override fun getExplorePage(): ExplorePage {
                val rowsFlow = flow {
                    val rows = mutableListOf<ExploreBooksRow>()
                    emitRowIfNotEmpty(rows, "Latest updates", fetchLatestNovels(DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "New arrivals", fetchRecentNovels(page = 0, size = DEFAULT_ROW_LIMIT * 2, limit = DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Completed gems", fetchCompletedNovels(DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Fantasy spotlight", fetchCategoryRandomNovels(CategoryEnum.FANTASY, DEFAULT_ROW_LIMIT))
                    emitRowIfNotEmpty(rows, "Romance spotlight", fetchCategoryRandomNovels(CategoryEnum.ROMANCE, DEFAULT_ROW_LIMIT))
                }
                return ExplorePage(title = title, rows = rowsFlow)
            }
        }
    }

    private suspend fun FlowCollector<List<ExploreBooksRow>>.emitRowIfNotEmpty(
        rows: MutableList<ExploreBooksRow>,
        title: String,
        books: List<ExploreDisplayBook>,
        expandable: Boolean = false,
        expandedId: String? = null
    ) {
        if (books.isEmpty()) return
        rows.add(
            ExploreBooksRow(
                title = title,
                bookList = books,
                expandable = expandable,
                expandedPageDataSourceId = expandedId
            )
        )
        emit(rows.toList())
    }

    private suspend fun fetchLatestNovels(limit: Int): List<ExploreDisplayBook> {
        return try {
            val response = apolloClient.query(LatestNovelsQuery()).execute()
            response.data?.latestNovels.orEmpty()
                .mapNotNull { novel ->
                    novel?.let {
                        convertToExploreDisplayBook(it.id, it.title, it.authorName, it.coverImage)
                    }
                }
                .take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchRecentNovels(page: Int, size: Int, limit: Int): List<ExploreDisplayBook> {
        return try {
            val response = apolloClient.query(
                RecentNovelsQuery(
                    page = page,
                    size = size
                )
            ).execute()
            response.data?.recentNovels?.content.orEmpty()
                .mapNotNull { novel ->
                    novel?.let {
                        convertToExploreDisplayBook(it.id, it.title, it.authorName, it.coverImage)
                    }
                }
                .take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchCompletedNovels(limit: Int): List<ExploreDisplayBook> {
        return try {
            val response = apolloClient.query(CompletedNovelsQuery()).execute()
            response.data?.completedNovels.orEmpty()
                .mapNotNull { novel ->
                    novel?.let {
                        convertToExploreDisplayBook(it.id, it.title, it.authorName, it.coverImage)
                    }
                }
                .take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchCategoryRandomNovels(category: CategoryEnum, limit: Int): List<ExploreDisplayBook> {
        return try {
            val response = apolloClient.query(
                CategoryRandomNovelsQuery(
                    category = category,
                    limit = Optional.Present(limit)
                )
            ).execute()
            response.data?.categoryRandomNovels.orEmpty()
                .mapNotNull { novel ->
                    novel?.let {
                        convertToExploreDisplayBook(it.id, it.title, it.authorName, it.coverImage)
                    }
                }
                .take(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    override fun stopAllSearch() {
        // No-op for now
    }
    
    override fun progressBookTagClick(tag: String, navController: NavController) {
        // TODO: Implement tag navigation if needed
    }
    
    // Conversion functions
    private fun convertToBookInformation(novel: GetNovelQuery.Novel): BookInformation {
        return convertToBookInformationCommon(
            id = novel.id,
            title = novel.title,
            coverImage = novel.coverImage,
            authorName = novel.authorName,
            description = novel.description,
            tags = novel.tags as List<String>?,
            wordCount = novel.wordCount,
            updatedAt = novel.updatedAt,
            status = novel.status?.name
        )
    }
    
    private fun convertToBookInformationFromSearch(novel: SearchNovelsQuery.SearchNovel): BookInformation {
        return convertToBookInformationCommon(
            id = novel.id,
            title = novel.title,
            coverImage = novel.coverImage,
            authorName = novel.authorName,
            description = novel.description,
            tags = novel.tags as List<String>?,
            wordCount = novel.wordCount,
            updatedAt = novel.updatedAt,
            status = novel.status?.name
        )
    }
    
    private fun convertToBookInformationCommon(
        id: String,
        title: String,
        coverImage: String?,
        authorName: String?,
        description: String?,
        tags: List<String>?,
        wordCount: Int?,
        updatedAt: String?,
        status: String?
    ): BookInformation {
        val coverUri = coverImage?.let { Uri.parse(it) } ?: Uri.EMPTY
        val lastUpdated = updatedAt?.let {
            try {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()
        
        val isComplete = status == "COMPLETED"
        
        return MutableBookInformation(
            id = id,
            title = title,
            subtitle = "",
            coverUrl = coverUri,
            author = authorName ?: "",
            description = description ?: "",
            tags = tags ?: emptyList(),
            publishingHouse = "",
            wordCount = WorldCount(wordCount ?: 0),
            lastUpdated = lastUpdated,
            isComplete = isComplete
        )
    }
    
    private suspend fun convertToVolume(volume: GetVolumesQuery.Volume): Volume? {
        return try {
            // Get chapters for this volume
            val chaptersResponse = apolloClient.query(GetChaptersQuery(volumeId = volume.id)).execute()
            
            val chapters = chaptersResponse.data?.chapters?.mapNotNull { chapter ->
                chapter?.let {
                    ChapterInformation(
                        id = it.id,
                        title = it.title
                    )
                }
            } ?: emptyList()
            
            Volume(
                volumeId = volume.id,
                volumeTitle = volume.title,
                chapters = chapters
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun convertToChapterContent(chapter: GetChapterQuery.Chapter): ChapterContent {
        val processedContent = chapter.content?.let { raw ->
            val version = chapter.version ?: 1
            if (version >= 2) HtmlToMdUtil.convertHtml(raw) else raw
        }
        val contentJson = processedContent?.takeIf { it.isNotBlank() }?.let { content ->
            ContentBuilder().apply {
                simpleText(content)
            }.build()
        } ?: buildJsonObject { }
        
        return MutableChapterContent(
            id = chapter.id,
            title = chapter.title,
            content = contentJson,
            lastChapter = chapter.previousChapterId ?: "",
            nextChapter = chapter.nextChapterId ?: ""
        )
    }

    private suspend fun searchByTitle(keyword: String): List<BookInformation> {
        val response = apolloClient.query(SearchNovelsQuery(title = keyword)).execute()
        return response.data?.searchNovels.orEmpty().mapNotNull { novel ->
            novel?.let { convertToBookInformationFromSearch(it) }
        }
    }

    private fun convertToExploreDisplayBook(
        id: String?,
        title: String?,
        authorName: String?,
        coverImage: String?
    ): ExploreDisplayBook? {
        if (id.isNullOrBlank() || title.isNullOrBlank()) return null
        val coverUri = coverImage?.takeIf { it.isNotBlank() }?.let {
            runCatching { Uri.parse(it) }.getOrElse { Uri.EMPTY }
        } ?: Uri.EMPTY
        return ExploreDisplayBook(
            id = id,
            title = title,
            author = authorName ?: "",
            coverUri = coverUri
        )
    }

    companion object {
        private const val SEARCH_TYPE_TITLE = "title"
        private const val DISCOVER_PAGE_ID = "discover"
        private const val DISCOVER_PAGE_TITLE = "Discover"
        private const val TRENDING_PAGE_ID = "trending"
        private const val TRENDING_PAGE_TITLE = "Trending"
        private const val GENRES_PAGE_ID = "genres"
        private const val GENRES_PAGE_TITLE = "Genres"
        private const val DEFAULT_ROW_LIMIT = 12
    }
}

