package com.miraimagiclab.novelreadingapp.ui.book.reader.content

import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.content.ContentData
import kotlinx.serialization.json.JsonObject

interface ContentViewModel {
    val uiState: ContentUiState
    fun changeBookId(id: String)
    fun loadNextChapter()
    fun loadLastChapter()
    fun changeChapter(id: String)

    companion object {
        class EmptyContentViewModel: ContentViewModel {
            override val uiState: ContentUiState = object: ContentUiState {
                override val bookId: String = ""
                override val readingChapterContent: ChapterContent = ChapterContent.empty()
                override val readingProgress: Float = 1f
                override val loadNextChapter: () -> Unit = {}
                override val loadLastChapter: () -> Unit = {}
                override val changeChapter: (String) -> Unit = {}
                override val getContentData: (JsonObject) -> ContentData = { ContentData.empty() }
            }

            override fun changeBookId(id: String) {
            }

            override fun loadNextChapter() {
            }

            override fun loadLastChapter() {
            }

            override fun changeChapter(id: String) {
            }

        }

        val empty = EmptyContentViewModel()
    }
}