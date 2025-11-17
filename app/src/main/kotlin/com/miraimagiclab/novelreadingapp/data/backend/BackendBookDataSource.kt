package com.miraimagiclab.novelreadingapp.data.backend

import android.net.Uri
import androidx.navigation.NavController
import com.miraimagiclab.novelreadingapp.network.GraphQLClient
import com.miraimagiclab.novelreadingapp.network.GraphQLQueries
import com.miraimagiclab.novelreadingapp.network.models.BackendChapter
import com.miraimagiclab.novelreadingapp.network.models.BackendNovel
import com.miraimagiclab.novelreadingapp.network.models.BackendVolume
import com.miraimagiclab.novelreadingapp.network.models.ChapterResponse
import com.miraimagiclab.novelreadingapp.network.models.ChaptersResponse
import com.miraimagiclab.novelreadingapp.network.models.NovelResponse
import com.miraimagiclab.novelreadingapp.network.models.SearchNovelsResponse
import com.miraimagiclab.novelreadingapp.network.models.VolumesResponse
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
import kotlinx.serialization.json.buildJsonObject
import io.lain4504.novelreadingapp.api.web.WebBookDataSource
import io.lain4504.novelreadingapp.api.web.WebDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExploreExpandedPageDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@WebDataSource(
    "Backend API",
    "Novel Reading Platform Backend"
)
@Singleton
class BackendBookDataSource @Inject constructor(
    private val graphQLClient: GraphQLClient
) : WebBookDataSource {
    
    override val id: Int = "backend-api".hashCode()
    
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
            val result = graphQLClient.query<NovelResponse>(
                query = GraphQLQueries.GET_NOVEL,
                variables = mapOf("id" to Json.parseToJsonElement("\"$id\""))
            )
            
            result.getOrNull()?.novel?.let { novel ->
                convertToBookInformation(novel)
            } ?: BookInformation.empty(id)
        } catch (e: Exception) {
            e.printStackTrace()
            BookInformation.empty(id)
        }
    }
    
    override suspend fun getBookVolumes(id: String): BookVolumes {
        return try {
            val result = graphQLClient.query<VolumesResponse>(
                query = GraphQLQueries.GET_VOLUMES,
                variables = mapOf("novelId" to Json.parseToJsonElement("\"$id\""))
            )
            
            val volumes = result.getOrNull()?.volumes?.map { convertToVolume(it) } ?: emptyList()
            BookVolumes(id, volumes)
        } catch (e: Exception) {
            e.printStackTrace()
            BookVolumes.empty(id)
        }
    }
    
    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent {
        return try {
            val result = graphQLClient.query<ChapterResponse>(
                query = GraphQLQueries.GET_CHAPTER,
                variables = mapOf("id" to Json.parseToJsonElement("\"$chapterId\""))
            )
            
            result.getOrNull()?.chapter?.let { chapter ->
                convertToChapterContent(chapter)
            } ?: ChapterContent.empty(chapterId)
        } catch (e: Exception) {
            e.printStackTrace()
            ChapterContent.empty(chapterId)
        }
    }
    
    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = flow {
        try {
            val result = graphQLClient.query<SearchNovelsResponse>(
                query = GraphQLQueries.SEARCH_NOVELS,
                variables = mapOf("title" to Json.parseToJsonElement("\"$keyword\""))
            )
            
            val novels = result.getOrNull()?.searchNovels?.map { convertToBookInformation(it) } ?: emptyList()
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
    private fun convertToBookInformation(novel: BackendNovel): BookInformation {
        val coverUri = novel.coverImage?.let { Uri.parse(it) } ?: Uri.EMPTY
        val lastUpdated = novel.updatedAt?.let {
            try {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()
        
        val isComplete = novel.status == "COMPLETED"
        
        return MutableBookInformation(
            id = novel.id,
            title = novel.title,
            subtitle = "",
            coverUrl = coverUri,
            author = novel.authorName ?: "",
            description = novel.description ?: "",
            tags = novel.tags ?: emptyList(),
            publishingHouse = "",
            wordCount = WorldCount(novel.wordCount ?: 0),
            lastUpdated = lastUpdated,
            isComplete = isComplete
        )
    }
    
    private suspend fun convertToVolume(volume: BackendVolume): Volume {
        // Get chapters for this volume
        val chaptersResult = graphQLClient.query<ChaptersResponse>(
            query = GraphQLQueries.GET_CHAPTERS,
            variables = mapOf("volumeId" to Json.parseToJsonElement("\"${volume.id}\""))
        )
        
        val chapters = chaptersResult.getOrNull()?.chapters?.map { chapter ->
            ChapterInformation(
                id = chapter.id,
                title = chapter.title
            )
        } ?: emptyList()
        
        return Volume(
            volumeId = volume.id,
            volumeTitle = volume.title,
            chapters = chapters
        )
    }
    
    private fun convertToChapterContent(chapter: BackendChapter): ChapterContent {
        // Convert chapter content to JsonObject format using ContentBuilder
        val contentJson = if (chapter.content != null) {
            ContentBuilder().apply {
                simpleText(chapter.content)
            }.build()
        } else {
            buildJsonObject { }
        }
        
        return MutableChapterContent(
            id = chapter.id,
            title = chapter.title,
            content = contentJson,
            lastChapter = chapter.previousChapterId ?: "",
            nextChapter = chapter.nextChapterId ?: ""
        )
    }
}

