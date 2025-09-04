package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookStatusIcon
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.isScrollingUp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onClickExportToEpub: (ExportSettings) -> Unit,
    onClickBackButton: () -> Unit,
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (String) -> Unit,
    onClickMarkAllRead: () -> Unit
) {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showExportBottomSheet by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    val exportBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                bookVolumes = uiState.bookVolumes,
                onClickBackButton = onClickBackButton,
                onClickExport = {
                    showExportBottomSheet = true
                },
                onClickTextFormatting = {
                    navController.navigateToSettingsTextFormattingRulesDestination(uiState.bookInformation.id)
                },
                onClickMarkAllRead = onClickMarkAllRead,
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        Content(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            onClickChapter = onClickChapter,
            onClickReadFromStart = onClickReadFromStart,
            onClickContinueReading = onClickContinueReading,
            cacheBook = cacheBook,
            requestAddBookToBookshelf = requestAddBookToBookshelf,
            onClickTag = onClickTag,
            onClickCover = onClickCover
        )
        AnimatedVisibility(visible = showExportBottomSheet) {
            ExportBottomSheet(
                sheetState = exportBottomSheetState,
                bookVolumes = uiState.bookVolumes,
                settings = exportSettings,
                onSettingsChange = { exportSettings = it },
                onDismissRequest = { showExportBottomSheet = false },
                onClickExport = onClickExportToEpub
            )
        }
    }
}

private val itemHorizontalPadding = 18.dp
private val itemVerticalPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (String) -> Unit
) {
    val infoBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var showInfoBottomSheet by remember { mutableStateOf(false) }
    var hideReadChapters by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val bookIsEmpty = uiState.bookInformation.title.isEmpty()

    val lazyListState = rememberLazyListState()
    val scrollOffset by remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }

    LaunchedEffect(Unit) {
        delay(50)
        showContent = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = bookIsEmpty,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(Modifier.fillMaxSize()) {
                Loading()
            }
        }
        AnimatedVisibility(
            visible = showContent && !bookIsEmpty,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    BookCardBlock(
                        bookInformation = uiState.bookInformation,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = scrollOffset * 0.5f
                            }
                            .fillMaxWidth(),
                        onClickCover = onClickCover
                    )
                }
                item {
                    TagsBlock(
                        bookInformation = uiState.bookInformation,
                        onClickTag = onClickTag
                    )
                }
                item {
                    QuickOperationsBlock(
                        uiState = uiState,
                        onClickAddToBookShelf = { requestAddBookToBookshelf(uiState.bookInformation.id) },
                        onClickCache = { cacheBook(uiState.bookInformation.id) },
                        onClickShowInfo = { showInfoBottomSheet = true }
                    )
                }
                item {
                    IntroBlock(uiState.bookInformation.description)
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.detail_contents),
                            style = AppTypography.titleLarge,
                            fontWeight = FontWeight.W600
                        )
                        AssistChip(
                            onClick = { hideReadChapters = !hideReadChapters },
                            label = {
                                Text(
                                    text = stringResource(
                                        if (hideReadChapters) R.string.show_read
                                        else R.string.hide_read
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.scale(0.75f),
                                    painter = painterResource(
                                        if (hideReadChapters) R.drawable.filled_menu_book_24px
                                        else R.drawable.done_all_24px
                                    ),
                                    contentDescription = "Toggle Hide Read"
                                )
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = uiState.bookVolumes.volumes.isEmpty(),
                        exit = shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Loading()
                        }
                    }
                }

                items(
                    uiState.bookVolumes.volumes,
                    key = { it.volumeId },
                    contentType = { "volume" }
                ) { volume ->
                    VolumeItem(
                        volume = volume,
                        hideReadChapters = hideReadChapters,
                        readCompletedChapterIds = uiState.userReadingData.readCompletedChapterIds,
                        onClickChapter = onClickChapter,
                        volumesSize = uiState.bookVolumes.volumes.size,
                        lastReadingChapterId = uiState.userReadingData.lastReadChapterId
                    )
                }

                item {
                    Spacer(Modifier.height(48.dp))
                }
            }

            AnimatedVisibility(
                visible = lazyListState.canScrollForward &&
                        lazyListState.isScrollingUp().value &&
                        uiState.bookVolumes.volumes.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 24.dp, bottom = 54.dp)
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        onClick = if (uiState.userReadingData.lastReadChapterId == -1) onClickReadFromStart
                        else onClickContinueReading,
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.filled_menu_book_24px),
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(if (uiState.userReadingData.lastReadChapterId == -1) stringResource(R.string.start_reading)
                            else stringResource(id = R.string.continue_reading))
                        }
                    )
                }
            }
        }
    }

    BookInfoBottomSheet(
        bookInformation = uiState.bookInformation,
        bookVolumes = uiState.bookVolumes,
        sheetState = infoBottomSheetState,
        isVisible = showInfoBottomSheet,
        onDismissRequest = { showInfoBottomSheet = false }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    bookVolumes: BookVolumes,
    onClickBackButton: () -> Unit,
    onClickExport: () -> Unit,
    onClickTextFormatting: () -> Unit,
    onClickMarkAllRead: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val volumesNotEmpty = bookVolumes.volumes.isNotEmpty()
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            LazyRow {
                item {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = AppTypography.titleTopBar,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                enabled = volumesNotEmpty,
                onClick = onClickExport
            ) {
                Icon(painterResource(id = R.drawable.file_export_24px), "export to epub")
            }
            IconButton(
                enabled = volumesNotEmpty,
                onClick = onClickTextFormatting
            ) {
                Icon(painterResource(id = R.drawable.find_replace_24px), "text formating")
            }
            Box {
                IconButton(
                    enabled = volumesNotEmpty,
                    onClick = { menuExpanded = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert_24px),
                        contentDescription = "more"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "全部标为已读",
                                style = AppTypography.dropDownItem
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onClickMarkAllRead()
                        }
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BookCardBlock(
    bookInformation: BookInformation,
    modifier: Modifier,
    onClickCover: (String) -> Unit
) {
    val updateText = if (bookInformation.isComplete) {
        stringResource(R.string.book_completed)
    } else {
        stringResource(
            R.string.book_info_update_date,
            bookInformation.lastUpdated.year,
            bookInformation.lastUpdated.monthValue,
            bookInformation.lastUpdated.dayOfMonth
        )
    }
    val wordCountText = stringResource(
        R.string.book_info_word_count_kilo,
        NumberFormat.getNumberInstance(Locale.getDefault()).format(bookInformation.wordCount / 1000)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(188.dp)
            .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable(
                    onClick = {
                        onClickCover(bookInformation.coverUrl)
                    }
                )
        ) {
            Cover(
                height = 178.dp,
                width = 122.dp,
                url = bookInformation.coverUrl,
                rounded = 8.dp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = bookInformation.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600,
                style = AppTypography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (bookInformation.subtitle.isNotEmpty()) {
                Text(
                    text = bookInformation.subtitle,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.secondary,
                    style = AppTypography.labelMedium
                )
            }
            Text(
                text = bookInformation.author,
                maxLines = 1,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.primary,
                style = AppTypography.labelLarge
            )
            Column {
                InfoRow(
                    icon = { BookStatusIcon(bookInformation.isComplete) },
                    text = updateText
                )
                Spacer(Modifier.height(2.dp))
                InfoRow(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.text_snippet_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                    },
                    text = wordCountText
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            maxLines = 1,
            style = AppTypography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


@Composable
private fun TagsBlock(
    bookInformation: BookInformation,
    onClickTag: (String) -> Unit
) {
    val fadingBrush = remember {
        Brush.horizontalGradient(
            0.02f to Color.Transparent,
            0.05f to Color.White,
            0.95f to Color.White,
            0.98f to Color.Transparent
        )
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .fadingEdge(fadingBrush)
            .padding(vertical = itemVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (bookInformation.publishingHouse.isNotEmpty()) {
            item(key = "publishingHouse") {
                SuggestionChip(
                    label = { Text(bookInformation.publishingHouse) },
                    onClick = {}
                )
            }
        }

        items(
            items = bookInformation.tags,
            key = { it }
        ) { tag ->
            SuggestionChip(
                label = { Text(tag) },
                onClick = { onClickTag(tag) }
            )
        }
    }
}

@Composable
fun QuickOperationButton(
    icon: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(0.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickOperationsBlock(
    uiState: DetailUiState,
    onClickAddToBookShelf: () -> Unit,
    onClickCache: () -> Unit,
    onClickShowInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding)
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.isInBookshelf) {
            QuickOperationButton(
                painterResource(R.drawable.filled_bookmark_add_24px),
                title = stringResource(R.string.activity_collections),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = painterResource(R.drawable.bookmark_add_24px),
                title = stringResource(R.string.add_to_bookshelf),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState.isCached) {
            QuickOperationButton(
                icon = painterResource(R.drawable.filled_cloud_download_24px),
                title = if (uiState.downloadItem == null || uiState.downloadItem!!.progress == 1f)
                    stringResource(R.string.cached)
                else
                    "${(uiState.downloadItem!!.progress * 100).toInt()}%",
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = painterResource(R.drawable.cloud_download_24px),
                title = if (uiState.downloadItem == null)
                    stringResource(R.string.cached_false)
                else
                    "${(uiState.downloadItem!!.progress * 100).toInt()}%",
                onClick = if (uiState.downloadItem == null) onClickCache else { {} },
                modifier = Modifier.weight(1f)
            )
        }

        QuickOperationButton(
            icon = painterResource(R.drawable.info_24px),
            title = stringResource(R.string.action_show_info),
            onClick = onClickShowInfo,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IntroBlock(description: String) {
    var overflowed by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.detail_introduction),
            style = AppTypography.titleLarge,
            fontWeight = FontWeight.W600
        )
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .fadingEdge(
                        if (!expanded && overflowed) Brush.verticalGradient(
                            0.7f to Color.White,
                            1f to Color.Transparent
                        )
                        else Brush.verticalGradient(listOf(Color.White, Color.White))
                    ),
                text = description,
                style = AppTypography.bodyLarge,
                maxLines = if (!expanded) 4 else 99,
                onTextLayout = {
                    overflowed = it.hasVisualOverflow || expanded
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (overflowed) {
            val rotation by animateFloatAsState(if (expanded) 0f else 180f)
            Button(
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors().copy(containerColor = Color.Transparent),
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    modifier = Modifier.rotate(rotation),
                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = "expand",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun VolumeItem(
    volume: Volume,
    hideReadChapters: Boolean = false,
    readCompletedChapterIds: List<Int>,
    onClickChapter: (Int) -> Unit,
    volumesSize: Int,
    lastReadingChapterId: Int
) {
    val readIds = remember(readCompletedChapterIds) { readCompletedChapterIds.toSet() }
    val readCount = volume.chapters.count { it.id in readIds }
    val totalCount = volume.chapters.size
    val isFullyRead = readCount >= totalCount

    var expanded by rememberSaveable {
        mutableStateOf(readCount < totalCount || volumesSize > 8)
    }

    if (hideReadChapters && isFullyRead) return
    val rotation by animateFloatAsState(if (expanded) 90f else 0f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable {
                    expanded = !expanded
                }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(5f)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = volume.volumeTitle,
                    fontWeight = FontWeight.W600,
                    style = AppTypography.titleMedium,
                    color = if (isFullyRead) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFullyRead) stringResource(R.string.info_reading_finished)
                    else stringResource(R.string.info_reading_progress, readCount, totalCount),
                    style = AppTypography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation),
                painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                contentDescription = "expand"
            )
            Spacer(Modifier.width(12.dp))
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(tween(150)),
            exit = fadeOut() + shrinkVertically(tween(150))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                volume.chapters.forEach { chapter ->
                    AnimatedVisibility(
                        visible = !(hideReadChapters && chapter.id in readIds),
                        enter = fadeIn() + expandVertically(tween(150)),
                        exit = fadeOut() + shrinkVertically(tween(150))
                    ) {
                        ChapterItem(
                            chapter = chapter,
                            isRead = chapter.id in readIds,
                            isLastRead = chapter.id == lastReadingChapterId,
                            onClick = { onClickChapter(chapter.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterInformation,
    isRead: Boolean,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = 12.dp,
                bottom = if (isLastRead) 6.dp else 12.dp
            )

    ) {
        Text(
            text = chapter.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = AppTypography.titleSmall,
            fontWeight = if (isRead) FontWeight.Normal else FontWeight.W600,
            color = if (isRead) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurface
        )
        if (isLastRead) {
            Text(
                text = stringResource(R.string.last_read),
                maxLines = 1,
                style = AppTypography.titleSmall,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
