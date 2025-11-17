package com.miraimagiclab.novelreadingapp.ui.book.reader.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.miraimagiclab.novelreadingapp.ui.book.reader.SettingState
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.flip.FlipPageContentComponent
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.flip.FlipPageContentUiState
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.scroll.ScrollContentComponent
import com.miraimagiclab.novelreadingapp.ui.book.reader.content.scroll.ScrollContentUiState

@Composable
fun ContentComponent(
    modifier: Modifier = Modifier,
    uiState: ContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onClickPrevChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    uiState.let { contentUiState ->
        when(contentUiState) {
            is FlipPageContentUiState -> FlipPageContentComponent(
                modifier,
                contentUiState,
                settingState,
                paddingValues,
                changeIsImmersive,
                onClickPrevChapter,
                onClickNextChapter
            )
            is ScrollContentUiState -> ScrollContentComponent(
                modifier,
                contentUiState,
                settingState,
                paddingValues,
                changeIsImmersive,
                onClickPrevChapter,
                onClickNextChapter,
            )
        }
    }
}