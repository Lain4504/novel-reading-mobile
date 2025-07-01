package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

@Stable
interface ContentUiState {
    val bookId: Int
    val readingChapterContent: ChapterContent
    val readingProgress: Float
    val loadNextChapter: () -> Unit
    val loadLastChapter: () -> Unit
    val changeChapter: (Int) -> Unit
}