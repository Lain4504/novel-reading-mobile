package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

interface ContentUiState {
    val bookId: Int
    val readingChapterContent: ChapterContent
    val readingProgress: Float
    val loadNextChapter: () -> Unit
    val loadLastChapter: () -> Unit
    val changeChapter: (Int) -> Unit
}