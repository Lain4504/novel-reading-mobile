package com.miraimagiclab.novelreadingapp.data.graphql

import android.net.Uri
import androidx.navigation.NavController
import com.apollographql.apollo.ApolloClient
import com.miraimagiclab.novelreadingapp.graphql.GetChapterQuery
import com.miraimagiclab.novelreadingapp.graphql.GetChaptersQuery
import com.miraimagiclab.novelreadingapp.graphql.GetNovelQuery
import com.miraimagiclab.novelreadingapp.graphql.GetVolumesQuery
import com.miraimagiclab.novelreadingapp.graphql.SearchNovelsQuery
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
import io.lain4504.novelreadingapp.api.web.WebBookDataSource
import io.lain4504.novelreadingapp.api.web.WebDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExploreExpandedPageDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
    
    override val id: Int = "graphql-apollo".hashCode()
    
    private val _offLine = MutableStateFlow(false)
    override val offLine: Boolean
        get() = _offLine.value
    
    override val isOffLineFlow: Flow<Boolean> = _offLine
    
    override val explorePageIdList: List<String> = emptyList()
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> = emptyMap()
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> = emptyMap()
    override val searchTypeMap: Map<String, String> = mapOf(
        "title" to "Title"
    )
    override val searchTipMap: Map<String, String> = mapOf(
        "title" to "Search by novel title"
    )
    override val searchTypeIdList: List<String> = listOf("title")
    
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
        try {
            val response = apolloClient.query(SearchNovelsQuery(title = keyword)).execute()
            
            val novels = response.data?.searchNovels?.mapNotNull { novel ->
                novel?.let { convertToBookInformationFromSearch(it) }
            } ?: emptyList()
            
            emit(novels)
            emit(listOf(BookInformation.empty())) // End marker
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
            emit(listOf(BookInformation.empty()))
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
        // Convert chapter content to JsonObject format using ContentBuilder
        val contentJson = chapter.content?.let { content ->
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
}

