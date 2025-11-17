package com.miraimagiclab.novelreadingapp.network.models

import kotlinx.serialization.Serializable

/**
 * Backend Novel Model (từ GraphQL)
 */
@Serializable
data class BackendNovel(
    val id: String,
    val title: String,
    val slug: String? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val largeBackground: String? = null,
    val categories: List<String>? = null,
    val tags: List<String>? = null,
    val viewCount: Int? = null,
    val followCount: Int? = null,
    val rating: Float? = null,
    val ratingCount: Int? = null,
    val wordCount: Int? = null,
    val chapterCount: Int? = null,
    val authorId: String,
    val authorName: String? = null,
    val illustratorName: String? = null,
    val status: String? = null,
    val type: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isR18: Boolean? = null
)

/**
 * Backend Volume Model
 */
@Serializable
data class BackendVolume(
    val id: String,
    val title: String,
    val novelId: String,
    val coverImage: String? = null,
    val volumeNumber: Int? = null,
    val chapterCount: Int? = null,
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Backend Chapter Model
 */
@Serializable
data class BackendChapter(
    val id: String,
    val title: String,
    val content: String? = null,
    val volumeId: String? = null,
    val novelId: String,
    val chapterNumber: Int? = null,
    val wordCount: Int? = null,
    val viewCount: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val previousChapterId: String? = null,
    val nextChapterId: String? = null
)

/**
 * GraphQL Response Wrapper
 */
@Serializable
data class NovelResponse(
    val novel: BackendNovel?
)

@Serializable
data class VolumesResponse(
    val volumes: List<BackendVolume>?
)

@Serializable
data class ChaptersResponse(
    val chapters: List<BackendChapter>?
)

@Serializable
data class ChapterResponse(
    val chapter: BackendChapter?
)

@Serializable
data class SearchNovelsResponse(
    val searchNovels: List<BackendNovel>?
)

