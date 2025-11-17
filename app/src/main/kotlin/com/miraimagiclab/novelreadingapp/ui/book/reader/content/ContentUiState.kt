package com.miraimagiclab.novelreadingapp.ui.book.reader.content

import androidx.compose.runtime.Stable
import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.content.ContentData
import kotlinx.serialization.json.JsonObject

@Stable
interface ContentUiState {
    val bookId: String
    val readingChapterContent: ChapterContent
    val readingProgress: Float
    val loadNextChapter: () -> Unit
    val loadLastChapter: () -> Unit
    val changeChapter: (String) -> Unit
    val getContentData: (JsonObject) -> ContentData
}