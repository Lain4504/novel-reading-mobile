package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookStatusIcon
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.isScrollingUp
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onClickExportToEpub: () -> Unit,
    onClickBackButton: () -> Unit,
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit,
    id: Int,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                onClickBackButton = onClickBackButton,
                onClickExport = {
                    onClickExportToEpub.invoke()
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Content(
                onClickChapter = onClickChapter,
                onClickReadFromStart = onClickReadFromStart,
                onClickContinueReading = onClickContinueReading,
                id = id,
                cacheBook = cacheBook,
                requestAddBookToBookshelf = requestAddBookToBookshelf
            )
        }
    }
}

private val itemHorizontalPadding = 18.dp
private val itemVerticalPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    viewModel: DetailViewModel = hiltViewModel(),
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit = {
        viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
            onClickChapter(it)
        }
    },
    onClickContinueReading: () -> Unit = {
        if (viewModel.uiState.userReadingData.lastReadChapterId == -1)
            viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                onClickChapter(it)
            }
        else
            onClickChapter(viewModel.uiState.userReadingData.lastReadChapterId)
    },
    id: Int,
    cacheBook: (Int) -> Unit,
    requestAddBookToBookshelf: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val infoBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var showInfoBottomSheet by remember { mutableStateOf(false) }
    var hideReadChapters by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    var scrolledY by remember { mutableFloatStateOf(0f) }
    var previousOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(id) {
        viewModel.init(id)
    }
    AnimatedVisibility(
        visible = viewModel.uiState.bookInformation.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    AnimatedVisibility(
        visible = !viewModel.uiState.bookInformation.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            item {
                BookCardBlock(
                    bookInformation = uiState.bookInformation,
                    modifier = Modifier.graphicsLayer {
                        scrolledY += lazyListState.firstVisibleItemScrollOffset - previousOffset
                        translationY = scrolledY * 0.5f
                        previousOffset = lazyListState.firstVisibleItemScrollOffset
                    }
                        .fillMaxWidth()
                )
            }
            item {
                TagsBlock(bookInformation = uiState.bookInformation)
            }
            item {
                QuickOperationsBlock(
                    onClickAddToBookShelf = { requestAddBookToBookshelf(uiState.bookInformation.id) },
                    onClickCache = { cacheBook(uiState.bookInformation.id) },
                    onClickShowInfo = { showInfoBottomSheet = true }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AssistChip(
                        onClick = { hideReadChapters = !hideReadChapters },
                        label = {
                            Text(
                                text = stringResource(
                                    if (hideReadChapters) R.string.show_read
                                    else R.string.hide_read))
                                },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.scale(0.75f, 0.75f),
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
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Loading()
                }
            }
            items(uiState.bookVolumes.volumes) {
                VolumeItem(
                    volume = it,
                    hideReadChapters = hideReadChapters,
                    readCompletedChapterIds = uiState.userReadingData.readCompletedChapterIds,
                    onClickChapter = onClickChapter,
                    volumesSize = uiState.bookVolumes.volumes.size
                )
            }
        }
        AnimatedVisibility(
            visible = lazyListState.isScrollingUp().value,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 31.dp, bottom = 54.dp)
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
    BookInfoBottomSheet(
        bookInformation = uiState.bookInformation,
        bookVolumes = uiState.bookVolumes,
        sheetState = infoBottomSheetState,
        isVisible = showInfoBottomSheet,
        onDismissRequest = {
            showInfoBottomSheet = false
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    onClickExport: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            LazyRow {
                item {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onClickExport
            ) {
                Icon(painterResource(id = R.drawable.file_export_24px), "export to epub")
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
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(188.dp)
            .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            height = 178.dp,
            width = 122.dp,
            url = bookInformation.coverUrl,
            rounded = 8.dp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .wrapContentHeight(Alignment.CenterVertically),
                text = bookInformation.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                lineHeight = 24.sp,
            )
            if (bookInformation.subtitle.isNotEmpty()) {
                Text(
                    text = bookInformation.subtitle,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 20.sp,
                    fontSize = 15.sp,
                )
            }
            Text(
                text = bookInformation.author,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 20.sp,
                fontSize = 16.sp,
            )
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookStatusIcon(bookInformation)
                    Text(
                        text = if (bookInformation.isComplete)
                            stringResource(R.string.book_completed)
                        else stringResource(
                            R.string.book_info_update_date,
                            bookInformation.lastUpdated.year,
                            bookInformation.lastUpdated.monthValue,
                            bookInformation.lastUpdated.dayOfMonth
                        ),
                        maxLines = 1,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp),
                        painter = painterResource(R.drawable.text_snippet_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = stringResource(
                            R.string.book_info_word_count_kilo,
                            bookInformation.wordCount / 1000
                        ),
                        maxLines = 1,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TagsBlock(
    bookInformation: BookInformation
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .fadingEdge(
                Brush.horizontalGradient(
                    0.02f to Color.Transparent,
                    0.05f to Color.White,
                    0.95f to Color.White,
                    0.98f to Color.Transparent
                )
            )
            .padding(vertical = itemVerticalPadding)
        ,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            if (bookInformation.publishingHouse.isNotEmpty()) {
                Spacer(Modifier.width(18.dp))
                SuggestionChip(
                    label = {
                        Text(bookInformation.publishingHouse)
                    },
                    onClick = {}
                )
            } else Spacer(Modifier.width(10 .dp))
        }
        items(bookInformation.tags) { tag ->
            SuggestionChip(
                label = {
                    Text(tag)
                },
                onClick = {}
            )
        }
        item {
            Spacer(Modifier.width(18.dp))
        }
    }
}

@Composable
private fun QuickOperationsBlock(
    onClickAddToBookShelf: () -> Unit,
    onClickCache: () -> Unit,
    onClickShowInfo: () -> Unit
) {
    @Composable
    fun QuickOperationButton(
        icon: Painter,
        title: String,
        onClick: () -> Unit,
    ) {
        Button(
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
                    maxLines = 1
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding)
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            QuickOperationButton(
                icon = painterResource(R.drawable.outline_book_24px),
                title = stringResource(R.string.add_to_bookshelf),
                onClick = onClickAddToBookShelf
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            QuickOperationButton(
                icon = painterResource(R.drawable.cloud_download_24px),
                title = stringResource(R.string.action_cache),
                onClick = onClickCache
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            QuickOperationButton(
                icon = painterResource(R.drawable.info_24px),
                title = stringResource(R.string.action_show_info),
                onClick = onClickShowInfo
            )
        }
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
            text = stringResource(R.string.detail_introduction),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
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
                fontSize = 15.sp,
                maxLines = if (!expanded) 4 else 99,
                onTextLayout = {
                    overflowed = it.hasVisualOverflow || expanded
                },
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (overflowed) {
            Button(
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors().copy(containerColor = Color.Transparent),
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    modifier = Modifier.rotate(if (expanded) 0f else 180f),
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
    volumesSize: Int
) {
    val readCount = volume.chapters.count { it.id in readCompletedChapterIds }
    val totalCount = volume.chapters.size
    var expanded by rememberSaveable {
        mutableStateOf(readCount < totalCount || volumesSize > 8)
    }
    val isFullyRead = readCount >= totalCount

    if (hideReadChapters && isFullyRead) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .height(54.dp)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = volume.volumeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(R.string.info_reading_finished),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .height(54.dp)
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = volume.volumeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isFullyRead) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFullyRead) stringResource(R.string.info_reading_finished)
                        else stringResource(R.string.info_reading_progress, readCount, totalCount),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (expanded) 90f else 0f),
                    painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                    contentDescription = "expand"
                )
                Spacer(Modifier.width(12.dp))
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    volume.chapters.forEach {
                        if (!(hideReadChapters && readCompletedChapterIds.contains(it.id))) {
                            Box(
                                modifier = Modifier
                                    .clickable { onClickChapter(it.id) }
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = it.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 15.sp,
                                    fontWeight =
                                    if (readCompletedChapterIds.contains(it.id))
                                        FontWeight.Normal
                                    else FontWeight.Bold,
                                    color =
                                    if (readCompletedChapterIds.contains(it.id))
                                        MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookInfoBottomSheet(
    bookInformation: BookInformation,
    bookVolumes: BookVolumes,
    sheetState: SheetState,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
) {
    @Composable
    fun InfoItem(
        title: String? = "",
        content: String,
        titleStyle: TextStyle,
        contentStyle: TextStyle,
        icon: Painter? = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.weight(3f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title!!,
                    style = titleStyle
                )
            }

            Row(
                modifier = Modifier.weight(7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                val clipboardManager = LocalClipboardManager.current

                Text(
                    text = content,
                    style = contentStyle,
                    modifier = Modifier.fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                clipboardManager.setText(AnnotatedString(content))
                                Toast.makeText(context, "内容已复制", Toast.LENGTH_SHORT).show()
                            },
                        )
                )
            }
        }
    }

    AnimatedVisibility(visible = isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            val titleStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            val contentStyle = TextStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                InfoItem(
                    title = "标题",
                    content = bookInformation.title,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.title_24px)
                )

                if (bookInformation.subtitle.isNotEmpty()) {
                    InfoItem(
                        content = bookInformation.subtitle,
                        titleStyle = titleStyle,
                        contentStyle = contentStyle,
                    )
                }

                InfoItem(
                    title = "ID",
                    content = bookInformation.id.toString(),
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.info_24px)
                )

                InfoItem(
                    title = "作者",
                    content = bookInformation.author,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.person_edit_24px)
                )

                InfoItem(
                    title = "文库",
                    content = bookInformation.publishingHouse,
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.text_snippet_24px)
                )

                InfoItem(
                    title = "标签",
                    content = bookInformation.tags.joinToString(separator = "，"),
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.tag_24px)
                )

                InfoItem(
                    title = "统计",
                    content = "${NumberFormat.getInstance().format(bookInformation.wordCount)} 字\n共计 ${bookVolumes.volumes.count()} 章节, ${bookVolumes.volumes.sumOf { volume -> volume.chapters.size}} 卷",
                    titleStyle = titleStyle,
                    contentStyle = contentStyle,
                    icon = painterResource(R.drawable.text_fields_24px)
                )
            }
        }
    }
}
