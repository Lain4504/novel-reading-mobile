package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.PreviewContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip.FlipPageContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll.ScrollContentUiState
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily

@Composable
fun ContentComponent(
    modifier: Modifier = Modifier,
    uiState: ContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
) {
    uiState.let {
        when(it) {
            is FlipPageContentUiState -> FlipPageContentComponent(modifier, it, settingState, paddingValues, changeIsImmersive)
            is ScrollContentUiState -> ScrollContentComponent(modifier, it, settingState, paddingValues, changeIsImmersive)
            is PreviewContentUiState -> {
                Box(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    uiState.readingChapterContent.content
                        .split("[image]")
                        .filter { it.isNotBlank() }
                        .forEach {
                            BaseContentComponent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
                                    ),
                                text = it,
                                fontSize = settingState.fontSize.sp,
                                fontLineHeight = settingState.fontLineHeight.sp,
                                fontWeight = FontWeight(settingState.fontWeigh.toInt()),
                                fontFamily = rememberReaderFontFamily(settingState),
                                color = if (settingState.textColor.isUnspecified) MaterialTheme.colorScheme.onBackground else settingState.textColor
                            )
                        }
                }
            }
        }
    }
}