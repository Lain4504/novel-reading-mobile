package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.BaseContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ImageLayoutInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.readerTextColor
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily

@Composable
fun ScrollContentComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onZoomImage: (String, ImageLayoutInfo, ImageLayoutInfo) -> Unit
) {
    ScrollContentTextComponent(
        modifier = modifier,
        uiState = uiState,
        settingState = settingState,
        paddingValues = paddingValues,
        changeIsImmersive = changeIsImmersive,
        onZoomImage = onZoomImage
    )
}

@Composable
fun ScrollContentTextComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
    onZoomImage: (String, ImageLayoutInfo, ImageLayoutInfo) -> Unit
) {
    val density = LocalDensity.current
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    val textColor = readerTextColor(settingState)
    val fontFamily = rememberReaderFontFamily(settingState)
    if (settingState.enableBackgroundImage && settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop) {
        // FIXME: why twice?
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) {
                    screenHeight.toDp()
                })
                .offset(y = with(density) {
                    ((uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0)?.offset
                        ?: 0) % screenHeight + screenHeight).toDp()
                }),
            painter = rememberReaderBackgroundPainter(settingState),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) {
                    screenHeight.toDp()
                })
                .offset(y = with(density) {
                    ((uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0)?.offset
                        ?: 0) % screenHeight).toDp()
                }),
            painter = rememberReaderBackgroundPainter(settingState),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        uiState.writeProgressRightNow()
    }
    LazyColumn(
        modifier = modifier
            .padding(paddingValues)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        changeIsImmersive.invoke()
                    }
                )
            }
            .onGloballyPositioned {
                uiState.setLazyColumnSize(it.size)
            },
        state = uiState.lazyListState,
    ) {
        items(
            items = uiState.contentList,
            key = { it.id }
        ) {
            Column(
                Modifier.defaultMinSize(
                    minHeight = with(density) {
                        screenHeight.toDp()
                    }
                )
            ) {
                if (settingState.isUsingContinuousScrolling) {
                    val titleRegex = Regex("^(第[一二三四五六七八九十]+卷)\\s+(.*)")
                    val matchResult = titleRegex.find(it.title)

                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 36.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (matchResult != null) {
                            val (volumeTitle, chapterTitle) = matchResult.destructured
                            Text(
                                text = volumeTitle,
                                textAlign = TextAlign.Center,
                                fontSize = (settingState.fontSize + 2).sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = fontFamily,
                                color = textColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                text = chapterTitle,
                                textAlign = TextAlign.Center,
                                fontSize = (settingState.fontSize + 6).sp,
                                lineHeight = (settingState.fontSize + settingState.fontLineHeight + 6).sp,
                                fontWeight = FontWeight((settingState.fontWeigh.toInt() + 100)),
                                fontFamily = fontFamily,
                                color = textColor
                            )
                        } else {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                text = it.title,
                                textAlign = TextAlign.Center,
                                fontSize = (settingState.fontSize + 6).sp,
                                lineHeight = (settingState.fontSize + settingState.fontLineHeight + 6).sp,
                                fontWeight = FontWeight((settingState.fontWeigh.toInt() + 100)),
                                fontFamily = fontFamily,
                                color = textColor
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.width(48.dp),
                                color = textColor
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                it.content
                    .split("[image]")
                    .filter { it.isNotBlank() }
                    .forEach {
                        BaseContentComponent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(
                                    minHeight = with(density) {
                                        screenHeight.toDp()
                                    }
                                ),
                            text = it,
                            fontSize = settingState.fontSize.sp,
                            fontLineHeight = settingState.fontLineHeight.sp,
                            fontWeight = FontWeight(settingState.fontWeigh.toInt()),
                            fontFamily = fontFamily,
                            color = textColor,
                            onZoomImage = onZoomImage
                        )
                    }
            }
        }
    }
}
