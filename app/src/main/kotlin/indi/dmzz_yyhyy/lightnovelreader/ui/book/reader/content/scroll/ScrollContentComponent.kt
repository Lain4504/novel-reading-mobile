package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil.compose.rememberAsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.BaseContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily

@Composable
fun ScrollContentComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit
) {
    ScrollContentTextComponent(
        modifier = modifier,
        uiState = uiState,
        settingState = settingState,
        paddingValues = paddingValues,
        changeIsImmersive = changeIsImmersive
    )
}

@SuppressLint("FrequentlyChangedStateReadInComposition")
@Composable
fun ScrollContentTextComponent(
    modifier: Modifier,
    uiState: ScrollContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit
) {
    val density = LocalDensity.current
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    if (settingState.enableBackgroundImage && settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop) {
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
            painter =
            if (settingState.backgroundImageUri.toString()
                    .isEmpty()
            ) painterResource(id = R.drawable.paper)
            else rememberAsyncImagePainter(settingState.backgroundImageUri),
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
            painter =
                if (settingState.backgroundImageUri.toString()
                        .isEmpty()
                ) painterResource(id = R.drawable.paper)
                else rememberAsyncImagePainter(settingState.backgroundImageUri),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        uiState.writeProgressRightNow()
    }
    AnimatedVisibility(
        uiState.contentList.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    AnimatedVisibility(
        uiState.contentList.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
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
                    if (settingState.isUsingContinuousScrolling)
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it.title,
                            fontSize = (settingState.fontSize + 10).sp,
                            lineHeight = (settingState.fontSize + settingState.fontLineHeight + 10).sp,
                            fontWeight = FontWeight((settingState.fontWeigh.toInt() + 100)),
                            fontFamily = rememberReaderFontFamily(settingState),
                            color = if (settingState.textColor.isUnspecified) MaterialTheme.colorScheme.onBackground else settingState.textColor
                        )
                    AnimatedVisibility(
                        it.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Loading()
                    }
                    AnimatedVisibility(
                        !it.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
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
                                        )
                                        .padding(
                                            start = paddingValues.calculateStartPadding(
                                                LayoutDirection.Ltr
                                            ),
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
}
