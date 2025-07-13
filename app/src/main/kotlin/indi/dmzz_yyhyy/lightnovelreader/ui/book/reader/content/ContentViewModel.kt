package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.PreviewContentUiState

interface ContentViewModel {
    val uiState: ContentUiState
    fun changeBookId(id: Int)
    fun loadNextChapter()
    fun loadLastChapter()
    fun changeChapter(id: Int)

    companion object {
        class EmptyContentViewModel: ContentViewModel {
            override val uiState: ContentUiState = PreviewContentUiState(-1, ChapterContent.empty())

            override fun changeBookId(id: Int) {
            }

            override fun loadNextChapter() {
            }

            override fun loadLastChapter() {
            }

            override fun changeChapter(id: Int) {
            }

        }

        val empty = EmptyContentViewModel()
    }
}