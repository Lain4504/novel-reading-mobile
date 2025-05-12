package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentComponent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderBackgroundPainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    onClickBackButton: () -> Unit,
    accumulateReadTime: (Int, Int) -> Unit,
    updateTotalReadingTime: (Int, Int) -> Unit,
    onClickLastChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeChapter: (Int) -> Unit,
    onClickChangeBackgroundColor: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isImmersive by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            isImmersive = false
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = !isImmersive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                TopBar(
                    onClickBackButton = onClickBackButton,
                    title = readingScreenUiState.contentUiState.readingChapterContent.title,
                    scrollBehavior
                )
            }
        },
        containerColor = if (settingState.backgroundColor.isUnspecified) MaterialTheme.colorScheme.background else settingState.backgroundColor
    ) { _ ->
        if (settingState.enableBackgroundImage) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberReaderBackgroundPainter(settingState),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Content(
            isImmersive = isImmersive,
            readingScreenUiState = readingScreenUiState,
            settingState = settingState,
            accumulateReadingTime = accumulateReadTime,
            updateTotalReadingTime = updateTotalReadingTime,
            onClickLastChapter = onClickLastChapter,
            onClickNextChapter = onClickNextChapter,
            onChangeChapter = onChangeChapter,
            onChangeIsImmersive = { isImmersive = !isImmersive },
            onClickChangeBackgroundColor = onClickChangeBackgroundColor,
            onClickChangeTextColor = onClickChangeTextColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    isImmersive: Boolean,
    readingScreenUiState: ReaderScreenUiState,
    settingState: SettingState,
    updateTotalReadingTime: (Int, Int) -> Unit,
    accumulateReadingTime: (Int, Int) -> Unit,
    onClickLastChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onChangeChapter: (Int) -> Unit,
    onChangeIsImmersive: () -> Unit,
    onClickChangeBackgroundColor: () -> Unit,
    onClickChangeTextColor: () -> Unit
) {
    val activity = LocalActivity.current as Activity
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val settingsBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val chaptersBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var isRunning by remember { mutableStateOf(false) }
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var showChapterSelectorBottomSheet by remember { mutableStateOf(false) }
    var totalReadingTime by remember { mutableIntStateOf(0) }
    var selectedVolumeId by remember { mutableIntStateOf(-1) }
    var lastUpdate by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(isImmersive) {
        val window = (context as ComponentActivity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        if (isImmersive) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    LaunchedEffect(readingScreenUiState.bookVolumes) {
        selectedVolumeId = readingScreenUiState.bookVolumes.volumes.firstOrNull { volume -> volume.chapters.any { it.id == readingScreenUiState.contentUiState.readingChapterContent.id } }?.volumeId ?: -1
    }
    LifecycleResumeEffect(Unit) {
        isRunning = true
        onPauseOrDispose {
            isRunning = false
            if (totalReadingTime <= 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
            } else {
                Log.e("ReaderScreen", "time counter error, time now is $totalReadingTime over 60s")
                Toast.makeText(context, "计时器错误, 请向开发者报告此错误,", Toast.LENGTH_SHORT).show()
            }
            totalReadingTime = 0
        }
    }
    LaunchedEffect(settingState.keepScreenOn) {
        if (settingState.keepScreenOn)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    LaunchedEffect(isRunning) {
        while (isRunning) {
            totalReadingTime += 1
            if (totalReadingTime > 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
                totalReadingTime = 0
            }
            delay(1000)
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            val now = LocalTime.now()
            val elapsed = 1
            accumulateReadingTime(readingScreenUiState.bookId, elapsed)
            lastUpdate = now
            delay(1000)
        }
    }

    LifecycleResumeEffect(Unit) {
        onPauseOrDispose {
            accumulateReadingTime(readingScreenUiState.bookId, -1)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (totalReadingTime <= 60) {
                updateTotalReadingTime(readingScreenUiState.bookId, totalReadingTime)
            } else {
                Log.e("ReaderScreen", "time counter error, time now is $totalReadingTime over 60s")
                Toast.makeText(context, "计时器错误, 请向开发者报告此错误,", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AnimatedVisibility(
        visible =  readingScreenUiState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = readingScreenUiState.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Loading()
        }
        AnimatedVisibility(
            visible = !readingScreenUiState.isLoading,
            enter = fadeIn() + scaleIn(initialScale = 0.7f),
            exit = fadeOut() + scaleOut(targetScale = 0.7f)
        ) {
            val isEnableIndicator = settingState.enableBatteryIndicator || settingState.enableTimeIndicator || settingState.enableReadingChapterProgressIndicator
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(
                    readingScreenUiState.contentUiState,
                    label = "ContentAnimate"
                ) { contentUiState ->
                    ContentComponent(
                        uiState = contentUiState,
                        settingState = settingState,
                        paddingValues = if (settingState.autoPadding)
                            with(density) {
                                PaddingValues(
                                    top = WindowInsets.safeContent.getTop(density).toDp(),
                                    bottom = if (isEnableIndicator) 46.dp else 12.dp,
                                    start = 16.dp,
                                    end = 16.dp
                                )
                            }
                        else PaddingValues(
                            top = settingState.topPadding.dp,
                            bottom = if (isEnableIndicator) (settingState.bottomPadding + 38).dp else settingState.bottomPadding.dp,
                            start = settingState.leftPadding.dp,
                            end = settingState.rightPadding.dp
                        ),
                        changeIsImmersive = onChangeIsImmersive,
                    )

                }
                AnimatedVisibility (
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = isEnableIndicator,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Indicator(
                        Modifier
                            .padding(
                                if (settingState.autoPadding)
                                    PaddingValues(
                                        bottom = 8.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    )
                                else PaddingValues(
                                    bottom = settingState.bottomPadding.dp,
                                    start = settingState.leftPadding.dp,
                                    end = settingState.rightPadding.dp
                                )
                            ),
                        enableBatteryIndicator = settingState.enableBatteryIndicator,
                        enableTimeIndicator = settingState.enableTimeIndicator,
                        enableChapterTitle = settingState.enableChapterTitleIndicator,
                        chapterTitle = readingScreenUiState.contentUiState.readingChapterContent.title,
                        enableReadingChapterProgressIndicator = settingState.enableReadingChapterProgressIndicator,
                        readingChapterProgress = readingScreenUiState.contentUiState.readingProgress
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = !isImmersive,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            BottomBar(
                chapterContent = readingScreenUiState.contentUiState.readingChapterContent,
                readingChapterProgress = readingScreenUiState.contentUiState.readingProgress,
                onClickLastChapter = onClickLastChapter,
                onClickNextChapter = onClickNextChapter,
                onClickSettings = { showSettingsBottomSheet = true },
                onClickChapterSelector = { showChapterSelectorBottomSheet = true },
            )
        }
        AnimatedVisibility(visible = showSettingsBottomSheet) {
            SettingsBottomSheet(
                sheetState = settingsBottomSheetState,
                onDismissRequest = {
                    coroutineScope.launch { settingsBottomSheetState.hide() }.invokeOnCompletion {
                        if (!settingsBottomSheetState.isVisible) {
                            showSettingsBottomSheet = false
                        }
                    }
                    showSettingsBottomSheet = false
                },
                contentUiState = readingScreenUiState.contentUiState,
                settingState = settingState,
                onClickChangeBackgroundColor = onClickChangeBackgroundColor,
                onClickChangeTextColor = onClickChangeTextColor
            )
        }
        ChapterSelectorBottomSheet(
            sheetState = chaptersBottomSheetState,
            display = showChapterSelectorBottomSheet,
            selectedVolumeId = selectedVolumeId,
            bookVolumes = readingScreenUiState.bookVolumes,
            readingChapterId = readingScreenUiState.contentUiState.readingChapterContent.id,
            onDismissRequest = {
                coroutineScope.launch { chaptersBottomSheetState.hide() }.invokeOnCompletion {
                    if (!chaptersBottomSheetState.isVisible) {
                        showChapterSelectorBottomSheet = false
                    }
                }
                showChapterSelectorBottomSheet = false
                selectedVolumeId = readingScreenUiState.bookVolumes.volumes.firstOrNull { volume -> volume.chapters.any { it.id == readingScreenUiState.contentUiState.readingChapterContent.id } }?.volumeId ?: -1
            },
            onClickChapter = onChangeChapter,
            onChangeSelectedVolumeId = {
                selectedVolumeId = it
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    title: String,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        title = {
            LazyRow {
                item {
                    AnimatedContent(title, label = "TitleAnimate") {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        /*actions = {
            IconButton(
                enabled = false,
                onClick = {
                    //TODO 全屏
                }) {
                Icon(
                    painter = painterResource(R.drawable.fullscreen_24px),
                    contentDescription = "fullscreen")
            }
        },*/
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BottomBar(
    chapterContent: ChapterContent,
    readingChapterProgress: Float,
    onClickLastChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onClickSettings: () -> Unit,
    onClickChapterSelector: () -> Unit
) {
    BottomAppBar {
        Box(
            Modifier
                .fillMaxHeight()
                .width(12.dp))
        IconButton(
            onClick = onClickLastChapter,
            enabled = chapterContent.hasLastChapter()
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24px),
                contentDescription = "lastChapter")
        }
        IconButton(
            enabled = false,
            onClick = {
                //TODO 添加至书签
            }) {
            Icon(
                painter = painterResource(R.drawable.outline_bookmark_24px),
                contentDescription = "mark")
        }
        IconButton(onClick = onClickChapterSelector) {
            Icon(painterResource(id = R.drawable.menu_24px), "menu")
        }
        IconButton(onClick = onClickSettings) {
            Icon(
                painter = painterResource(R.drawable.outline_settings_24px),
                contentDescription = "setting")
        }
        Box(
            Modifier
                .padding(9.dp, 12.dp)
                .weight(2f)) {
            Box(Modifier.clip(ButtonDefaults.shape)) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(24.dp, 11.5.dp)
                ) {
                    AnimatedText(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${(readingChapterProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        IconButton(
            onClick = onClickNextChapter,
            enabled = chapterContent.hasNextChapter()
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_forward_24px),
                contentDescription = "nextChapter")
        }
        Box(
            Modifier
                .fillMaxHeight()
                .width(12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectorBottomSheet(
    sheetState: SheetState,
    display: Boolean,
    selectedVolumeId: Int,
    bookVolumes: BookVolumes,
    readingChapterId: Int,
    onDismissRequest: () -> Unit,
    onClickChapter: (Int) -> Unit,
    onChangeSelectedVolumeId: (Int) -> Unit
) {
    val lazyColumnState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val indexedItems = bookVolumes.volumes.flatMap { volume ->
        listOf(volume to null) + volume.chapters.map { volume to it }
    }

    val localDensity = LocalDensity.current
    var columnHeightDp by remember {
        mutableStateOf(0.dp)
    }

    LaunchedEffect(sheetState.currentValue == PartiallyExpanded && sheetState.currentValue != Expanded) {
        val targetIndex = indexedItems.indexOfFirst { (_, chapter) ->
            chapter?.id == readingChapterId
        }
        if (targetIndex >= 0) {
            coroutineScope.launch {
                lazyColumnState.animateScrollToItem(targetIndex)
            }
        }
    }

    AnimatedVisibility(visible = display) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.read_more_24px),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.select_chapter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600
                )
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                },
                state = lazyColumnState
            ) {
                items(indexedItems) { (volume, chapter) ->
                    if (chapter == null) {
                        Box(
                            modifier = Modifier
                                .clickable {
                                    onChangeSelectedVolumeId(
                                        if (selectedVolumeId == volume.volumeId) -1 else volume.volumeId
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = volume.volumeTitle,
                                        fontWeight = FontWeight.W600,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.info_volume_chapters_count,
                                            volume.chapters.size
                                        ),
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 14.sp
                                    )
                                }
                                Box(Modifier.weight(2f))
                                Icon(
                                    modifier = Modifier
                                        .scale(0.75f, 0.75f)
                                        .rotate(if (selectedVolumeId == volume.volumeId) -90f else 90f),
                                    painter = painterResource(R.drawable.arrow_forward_ios_24px),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }
                    } else {
                        AnimatedVisibility(
                            visible = selectedVolumeId == volume.volumeId,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clickable { onClickChapter(chapter.id) }
                                        .padding(horizontal = 22.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    val isSelected = readingChapterId == chapter.id
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Icon(
                                                painter = painterResource(R.drawable.play_arrow_24px),
                                                tint = MaterialTheme.colorScheme.outline,
                                                contentDescription = null
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = chapter.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    enableBatteryIndicator: Boolean,
    enableTimeIndicator: Boolean,
    enableChapterTitle: Boolean,
    chapterTitle: String,
    enableReadingChapterProgressIndicator: Boolean,
    readingChapterProgress: Float
) {
    val current = LocalDensity.current
    val batteryManager = LocalContext.current.getSystemService(BATTERY_SERVICE) as BatteryManager
    val batLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    var progressIndicatorWidth by remember { mutableStateOf(0.dp) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (enableBatteryIndicator)
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter =
                    when {
                        (batLevel == 0) -> painterResource(R.drawable.battery_horiz_000_24px)
                        (batLevel in 1..10) -> painterResource(R.drawable.battery_very_low_24px)
                        (batLevel in 11..35) -> painterResource(R.drawable.battery_low_24px)
                        (batLevel in 36..65) -> painterResource(R.drawable.battery_horiz_050_24px)
                        (batLevel in 66..90) -> painterResource(R.drawable.battery_horiz_075_24px)
                        (batLevel in 91..100) -> painterResource(R.drawable.battery_full_alt_24px)
                        else -> painterResource(R.drawable.battery_horiz_000_24px)
                    },
                    contentDescription = null
                )
            if (enableTimeIndicator)
                AnimatedText(
                    text = "${LocalDateTime.now().hour} : " + LocalDateTime.now().minute.let { if (it < 10) "0$it" else it.toString() },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
        Box(Modifier.width(12.dp))
        Box {
            if (enableChapterTitle)
                LazyRow(
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = progressIndicatorWidth + 12.dp)) {
                    item {
                        AnimatedText(
                            text = chapterTitle,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.W500
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            if (enableReadingChapterProgressIndicator) {
                AnimatedText(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .onGloballyPositioned { layoutCoordinates ->
                            with(current) {
                                progressIndicatorWidth = layoutCoordinates.size.width.toDp()
                            }
                        },
                    text = "${(readingChapterProgress * 100).toInt()}%",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}