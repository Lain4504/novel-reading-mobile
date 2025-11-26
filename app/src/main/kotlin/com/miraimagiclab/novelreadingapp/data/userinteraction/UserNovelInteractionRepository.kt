package com.miraimagiclab.novelreadingapp.data.userinteraction

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.miraimagiclab.novelreadingapp.graphql.FollowNovelMutation
import com.miraimagiclab.novelreadingapp.graphql.FollowedNovelsPaginatedQuery
import com.miraimagiclab.novelreadingapp.graphql.MyReadingHistoryByNovelIdQuery
import com.miraimagiclab.novelreadingapp.graphql.RemoveFromReadingHistoryMutation
import com.miraimagiclab.novelreadingapp.graphql.UnfollowNovelMutation
import com.miraimagiclab.novelreadingapp.graphql.UpdateReadingProgressMutation
import com.miraimagiclab.novelreadingapp.graphql.UserNovelInteractionQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserNovelInteractionRepository @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun getUserNovelInteraction(novelId: String): Result<UserNovelInteractionData> = runCatching {
        val response = apolloClient.query(
            UserNovelInteractionQuery(novelId = novelId)
        ).execute()
        
        response.data?.userNovelInteraction?.let { interaction ->
            UserNovelInteractionData(
                id = interaction.id,
                userId = interaction.userId,
                novelId = interaction.novelId,
                hasFollowing = interaction.hasFollowing,
                inWishlist = interaction.inWishlist,
                hasRating = interaction.hasRating,
                rating = interaction.rating,
                currentChapterNumber = interaction.currentChapterNumber,
                currentChapterId = interaction.currentChapterId,
                totalChapterReads = interaction.totalChapterReads,
                lastReadAt = interaction.lastReadAt,
                notify = interaction.notify
            )
        } ?: throw IllegalStateException("No interaction data found")
    }

    suspend fun getFollowedNovelsPaginated(
        page: Int = 0,
        size: Int = 20
    ): Result<FollowedNovelsPaginatedData> = runCatching {
        val response = apolloClient.query(
            FollowedNovelsPaginatedQuery(
                page = Optional.presentIfNotNull(page),
                size = Optional.presentIfNotNull(size)
            )
        ).execute()
        
        response.data?.followedNovelsPaginated?.let { paged ->
            FollowedNovelsPaginatedData(
                content = paged.content.mapNotNull { novel ->
                    novel?.let {
                        FollowedNovelData(
                            id = it.id,
                            title = it.title ?: "",
                            description = it.description,
                            coverImage = it.coverImage,
                            authorId = it.authorId,
                            authorName = it.authorName,
                            categories = it.categories ?: emptyList(),
                            tags = it.tags ?: emptyList(),
                            rating = it.rating?.toDouble() ?: 0.0,
                            ratingCount = it.ratingCount ?: 0,
                            viewCount = it.viewCount ?: 0,
                            followCount = it.followCount ?: 0,
                            wishlistCount = it.wishlistCount ?: 0,
                            commentCount = it.commentCount ?: 0,
                            wordCount = it.wordCount ?: 0,
                            chapterCount = it.chapterCount ?: 0,
                            status = it.status?.name,
                            type = it.type?.name,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                            isR18 = it.isR18 ?: false,
                            latestChapterId = it.latestChapterId,
                            latestChapterTitle = it.latestChapterTitle,
                            latestChapterNumber = it.latestChapterNumber,
                            latestChapterCreatedAt = it.latestChapterCreatedAt
                        )
                    }
                },
                totalPages = paged.totalPages,
                totalElements = paged.totalElements,
                hasNext = paged.hasNext,
                hasPrevious = paged.hasPrevious
            )
        } ?: throw IllegalStateException("No followed novels data found")
    }

    suspend fun getMyReadingHistoryByNovelId(novelId: String): Result<ReadingHistoryData?> = runCatching {
        val response = apolloClient.query(
            MyReadingHistoryByNovelIdQuery(novelId = novelId)
        ).execute()
        
        response.data?.myReadingHistoryByNovelId?.let { history ->
            ReadingHistoryData(
                id = history.id,
                userId = history.userId,
                novelId = history.novelId,
                currentChapterNumber = history.currentChapterNumber,
                currentChapterId = history.currentChapterId,
                lastReadAt = history.lastReadAt
            )
        }
    }

    suspend fun followNovel(novelId: String): Result<FollowNovelResult> = runCatching {
        val response = apolloClient.mutation(
            FollowNovelMutation(novelId = novelId)
        ).execute()
        
        response.data?.followNovel?.let { result ->
            FollowNovelResult(
                success = result.success,
                message = result.message ?: "",
                hasFollowing = result.interaction?.hasFollowing ?: false
            )
        } ?: throw IllegalStateException("No follow result found")
    }

    suspend fun unfollowNovel(novelId: String): Result<UnfollowNovelResult> = runCatching {
        val response = apolloClient.mutation(
            UnfollowNovelMutation(novelId = novelId)
        ).execute()
        
        response.data?.unfollowNovel?.let { result ->
            UnfollowNovelResult(
                success = result.success,
                message = result.message ?: ""
            )
        } ?: throw IllegalStateException("No unfollow result found")
    }

    suspend fun updateReadingProgress(
        novelId: String,
        chapterNumber: Int
    ): Result<UpdateReadingProgressResult> = runCatching {
        val response = apolloClient.mutation(
            UpdateReadingProgressMutation(
                novelId = novelId,
                chapterNumber = chapterNumber
            )
        ).execute()
        
        response.data?.updateReadingProgress?.let { result ->
            UpdateReadingProgressResult(
                success = result.success,
                message = result.message ?: "",
                currentChapterNumber = result.interaction?.currentChapterNumber,
                currentChapterId = result.interaction?.currentChapterId,
                lastReadAt = result.interaction?.lastReadAt,
                totalChapterReads = result.interaction?.totalChapterReads
            )
        } ?: throw IllegalStateException("No update reading progress result found")
    }

    suspend fun removeFromReadingHistory(novelId: String): Result<RemoveFromReadingHistoryResult> = runCatching {
        val response = apolloClient.mutation(
            RemoveFromReadingHistoryMutation(novelId = novelId)
        ).execute()
        
        response.data?.removeFromReadingHistory?.let { result ->
            RemoveFromReadingHistoryResult(
                success = result.success,
                message = result.message ?: ""
            )
        } ?: throw IllegalStateException("No remove from reading history result found")
    }
}

// Data classes for results
data class UserNovelInteractionData(
    val id: String,
    val userId: String,
    val novelId: String,
    val hasFollowing: Boolean,
    val inWishlist: Boolean,
    val hasRating: Boolean,
    val rating: Int?,
    val currentChapterNumber: Int?,
    val currentChapterId: String?,
    val totalChapterReads: Int?,
    val lastReadAt: String?,
    val notify: Boolean
)

data class FollowedNovelsPaginatedData(
    val content: List<FollowedNovelData>,
    val totalPages: Int,
    val totalElements: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class FollowedNovelData(
    val id: String,
    val title: String,
    val description: String?,
    val coverImage: String?,
    val authorId: String,
    val authorName: String?,
    val categories: List<String>,
    val tags: List<String?>,
    val rating: Double,
    val ratingCount: Int,
    val viewCount: Int,
    val followCount: Int,
    val wishlistCount: Int,
    val commentCount: Int,
    val wordCount: Int,
    val chapterCount: Int,
    val status: String?,
    val type: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val isR18: Boolean,
    val latestChapterId: String?,
    val latestChapterTitle: String?,
    val latestChapterNumber: Int?,
    val latestChapterCreatedAt: String?
)

data class ReadingHistoryData(
    val id: String,
    val userId: String,
    val novelId: String,
    val currentChapterNumber: Int?,
    val currentChapterId: String?,
    val lastReadAt: String?
)

data class FollowNovelResult(
    val success: Boolean,
    val message: String,
    val hasFollowing: Boolean
)

data class UnfollowNovelResult(
    val success: Boolean,
    val message: String
)

data class UpdateReadingProgressResult(
    val success: Boolean,
    val message: String,
    val currentChapterNumber: Int?,
    val currentChapterId: String?,
    val lastReadAt: String?,
    val totalChapterReads: Int?
)

data class RemoveFromReadingHistoryResult(
    val success: Boolean,
    val message: String
)

