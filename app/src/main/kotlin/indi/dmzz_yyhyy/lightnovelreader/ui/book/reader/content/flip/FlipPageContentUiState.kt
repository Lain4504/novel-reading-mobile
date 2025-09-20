package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentUiState

interface FlipPageContentUiState: ContentUiState {
    val updatePageState: (PagerState) -> Unit
    val pagerState: PagerState
}

class MutableFlipPageContentUiState(
    override val loadNextChapter: () -> Unit,
    override val loadLastChapter: () -> Unit,
    override val changeChapter: (Int) -> Unit,
    override val updatePageState: (PagerState) -> Unit
): FlipPageContentUiState {
    override var pagerState by mutableStateOf(PagerState { 0 })
    override var bookId by mutableIntStateOf(-1)
    override var readingChapterContent: ChapterContent by mutableStateOf(ChapterContent.empty())
    override var readingProgress by mutableFloatStateOf(0f)
}