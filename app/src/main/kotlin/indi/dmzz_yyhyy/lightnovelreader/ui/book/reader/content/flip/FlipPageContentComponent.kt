package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.compose.rememberAsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.AppEvent
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.BaseContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun FlipPageContentComponent(
    modifier: Modifier,
    uiState: FlipPageContentUiState,
    settingState: SettingState,
    paddingValues: PaddingValues,
    changeIsImmersive: () -> Unit,
) {
    SimpleFlipPageTextComponent(
        modifier = modifier,
        paddingValues = paddingValues,
        uiState = uiState,
        settingState = settingState,
        changeIsImmersive = changeIsImmersive,
    )
}

@Composable
private fun SimpleFlipPageTextComponent(
    modifier: Modifier,
    paddingValues: PaddingValues,
    uiState: FlipPageContentUiState,
    settingState: SettingState,
    changeIsImmersive: () -> Unit,
) {
    val content = uiState.readingChapterContent.content
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val current = LocalContext.current
    var contentKey by remember { mutableIntStateOf(0) }
    var slipTextJob by remember { mutableStateOf<Job?>(null) }
    var constraints by remember { mutableStateOf<Constraints?>(null) }
    var textStyle by remember { mutableStateOf<TextStyle?>(null) }
    var slippedTextList by remember { mutableStateOf(emptyList<String>()) }
    var readingPageFistCharOffset by remember { mutableIntStateOf(0) }
    fun lastPage(pagerState: PagerState) {
        println(pagerState.currentPage)
        if (pagerState.currentPage != 0)
            scope.launch {
                if (settingState.flipAnime != MenuOptions.FlipAnimationOptions.None)
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                else
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                println(pagerState.currentPage)
            }
        else if (settingState.fastChapterChange && slippedTextList.isNotEmpty()) uiState.loadLastChapter.invoke()
    }

    fun nextPage(pagerState: PagerState) {
        println("fun:$pagerState")
        if (pagerState.currentPage + 1 < pagerState.pageCount)
            scope.launch {
                if (settingState.flipAnime != MenuOptions.FlipAnimationOptions.None)
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                else
                    pagerState.scrollToPage(pagerState.currentPage + 1)
            }
        else if (settingState.fastChapterChange && slippedTextList.isNotEmpty()) uiState.loadNextChapter.invoke()
    }

    LaunchedEffect(
        content,
        textStyle,
        settingState.fontLineHeight.sp,
        settingState.fontSize.sp,
        constraints?.maxHeight,
        constraints?.maxWidth
    ) {
        val key =
            content.hashCode() + settingState.fontLineHeight.sp.value.hashCode() + settingState.fontSize.sp.value.hashCode() + constraints?.maxHeight.hashCode() + constraints?.maxWidth.hashCode()
        if (constraints == null || textStyle == null || key == contentKey) return@LaunchedEffect
        contentKey = key
        slipTextJob?.cancel()
        slipTextJob = scope.launch(Dispatchers.IO) {
            readingPageFistCharOffset = slippedTextList
                .subList(0, uiState.pagerState.currentPage.coerceAtMost(slippedTextList.size))
                .sumOf { it.length }
                .plus(1)
            slippedTextList = slipText(
                textMeasurer = textMeasurer,
                constraints = constraints!!,
                text = content,
                style = textStyle!!.copy(
                    fontSize = settingState.fontSize.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = (settingState.fontLineHeight.sp.value + settingState.fontSize.sp.value).sp
                )
            )
            uiState.updatePageState(PagerState { slippedTextList.size })
        }
    }
    DisposableEffect(
        settingState.isUsingVolumeKeyFlip,
        settingState.flipAnime,
        settingState.fastChapterChange
    ) {
        val localBroadcastManager = LocalBroadcastManager.getInstance(current)
        val keycodeVolumeUpReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (settingState.isUsingVolumeKeyFlip)
                    lastPage(uiState.pagerState)
            }
        }
        val keycodeVolumeDownReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (settingState.isUsingVolumeKeyFlip)
                    nextPage(uiState.pagerState)
            }
        }
        localBroadcastManager.registerReceiver(
            keycodeVolumeUpReceiver,
            IntentFilter(AppEvent.KEYCODE_VOLUME_UP)
        )
        localBroadcastManager.registerReceiver(
            keycodeVolumeDownReceiver,
            IntentFilter(AppEvent.KEYCODE_VOLUME_DOWN)
        )
        onDispose {
            localBroadcastManager.unregisterReceiver(keycodeVolumeUpReceiver)
            localBroadcastManager.unregisterReceiver(keycodeVolumeDownReceiver)
        }
    }
    LocalContext.current.resources.displayMetrics.let { displayMetrics ->
        constraints = Constraints(
            maxWidth = displayMetrics
                .widthPixels
                .minus(
                    with(LocalDensity.current) {
                        (paddingValues.calculateStartPadding(LayoutDirection.Ltr) + paddingValues.calculateEndPadding(
                            LayoutDirection.Ltr
                        ))
                            .toPx()
                    }.toInt()
                ),
            maxHeight = displayMetrics
                .heightPixels
                .minus(
                    with(LocalDensity.current) {
                        (paddingValues.calculateTopPadding() + paddingValues.calculateBottomPadding() + 10.dp)
                            .toPx()
                    }.toInt()
                ),
        )
    }
    textStyle = MaterialTheme.typography.bodyMedium
    HorizontalPager(
        state = uiState.pagerState,
        modifier = modifier
            .draggable(
                enabled = settingState.isUsingFlipPage,
                interactionSource = remember { MutableInteractionSource() },
                orientation = Orientation.Vertical,
                state = rememberDraggableState {},
                onDragStopped = {
                    if (it.absoluteValue > 60) changeIsImmersive.invoke()
                }
            )
            .pointerInput(
                settingState.isUsingFlipPage,
                settingState.flipAnime,
                settingState.fastChapterChange
            ) {
                detectTapGestures(
                    onTap = {
                        println("wasda")
                        if (settingState.isUsingFlipPage)
                            if (it.x <= current.resources.displayMetrics.widthPixels * 0.425) lastPage(
                                uiState.pagerState
                            )
                            else nextPage(uiState.pagerState)
                        else changeIsImmersive.invoke()
                    }
                )
            },
    ) {
        println("ui:${uiState.pagerState}")
        Box(Modifier.fillMaxSize()) {
            if (settingState.enableBackgroundImage && settingState.backgroundImageDisplayMode == MenuOptions.ReaderBgImageDisplayModeOptions.Loop) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter =
                    if (settingState.backgroundImageUri.toString()
                            .isEmpty()
                    ) painterResource(id = R.drawable.paper)
                    else rememberAsyncImagePainter(settingState.backgroundImageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            BaseContentComponent(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                text = slippedTextList.getOrNull(it) ?: "",
                fontSize = settingState.fontSize.sp,
                fontLineHeight = settingState.fontLineHeight.sp,
                fontWeight = FontWeight(settingState.fontWeigh.toInt()),
                fontFamily = if (settingState.fontFamilyUri.toString()
                        .isEmpty()
                ) MaterialTheme.typography.bodyMedium.fontFamily else FontFamily(
                    Font(settingState.fontFamilyUri.toFile())
                ),
                color = if (settingState.textColor.isUnspecified) MaterialTheme.colorScheme.onBackground else settingState.textColor
            )
        }
    }
}

fun slipText(
    textMeasurer: TextMeasurer,
    constraints: Constraints,
    text: String,
    style: TextStyle,
): List<String> {
    val resultList: MutableList<String> = mutableListOf()
    text.split("[image]").filter { it.isNotEmpty() }.forEach { single ->
        if (single.trim().startsWith("http://") || single.trim().startsWith("https://"))
            resultList.add(single)
        else {
            textMeasurer
                .measure(
                    text = single,
                    style = style,
                    constraints = constraints
                )
                .getSlipString(single, constraints)
                .let(resultList::addAll)
        }
    }
    return resultList
}

fun TextLayoutResult.getSlipString(text: String, constraints: Constraints): List<String> {
    val result: MutableList<String> = mutableListOf()
    var lastLine = 0
    fun getNotOverflowText(startLine: Int): String {
        fun getNotOverflowLine(): Int {
            val startHeight = getLineTop(startLine)
            fun isLineOverflow(line: Int): Boolean =
                getLineBottom(line) > startHeight + constraints.maxHeight

            var checkLine = getLineForOffset(
                getOffsetForPosition(
                    Offset(
                        constraints.maxWidth.toFloat(),
                        startHeight + constraints.maxHeight
                    )
                )
            )
            while (isLineOverflow(checkLine))
                checkLine--
            return checkLine
        }

        val startTextOffset = getLineStart(startLine)
        lastLine = getNotOverflowLine()
        val endTextOffset = getLineEnd(lastLine)
        lastLine++
        return text.slice(startTextOffset..<endTextOffset)
    }
    while (lastLine != this.lineCount) {
        getNotOverflowText(lastLine).let(result::add)
    }
    return result.filter { it.isNotBlank() }
}