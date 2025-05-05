package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentUiState

@Composable
fun ContentComponent(
    modifier: Modifier = Modifier,
    uiState: ContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
) {
    when(uiState) {
        is FlipPageContentUiState -> FlipPageContentComponent(modifier, uiState, settingState, paddingValues, changeIsImmersive)
        is ScrollContentUiState -> ScrollContentComponent(modifier, uiState, settingState, paddingValues, changeIsImmersive)
    }
}