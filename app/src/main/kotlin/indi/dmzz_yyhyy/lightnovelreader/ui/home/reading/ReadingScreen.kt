package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.ui.SharedContentKey
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.HomeNavigateBar
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReadingScreen(
    controller: NavController,
    selectedRoute: Any,
    updateReadingBooks: () -> Unit,
    recentReadingBookInformationMap: Map<Int, BookInformation>,
    recentReadingUserReadingDataMap: Map<Int, UserReadingData>,
    recentReadingBookIds: List<Int>,
    onClickBook: (Int) -> Unit,
    onClickContinueReading: (Int, Int) -> Unit,
    onClickJumpToExploration: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        updateReadingBooks()
    }
    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopBar(TopAppBarDefaults.pinnedScrollBehavior())
            },
            bottomBar = {
                HomeNavigateBar(
                    modifier = Modifier.sharedElement(
                        sharedTransitionScope.rememberSharedContentState(SharedContentKey.HomeNavigateBar),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    selectedRoute = selectedRoute,
                    controller = controller
                )
            }
        ) {
            Box(Modifier.padding(it)) {
                ReadingContent(
                    onClickBook = onClickBook,
                    onClickContinueReading = onClickContinueReading,
                    onClickJumpToExploration = onClickJumpToExploration,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    recentReadingBookInformationMap = recentReadingBookInformationMap,
                    recentReadingUserReadingDataMap = recentReadingUserReadingDataMap,
                    recentReadingBookIds = recentReadingBookIds
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ReadingContent(
    onClickBook: (Int) -> Unit,
    onClickContinueReading: (Int, Int) -> Unit,
    onClickJumpToExploration: () -> Unit,
    recentReadingBookInformationMap: Map<Int, BookInformation>,
    recentReadingUserReadingDataMap: Map<Int, UserReadingData>,
    recentReadingBookIds: List<Int>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .nestedScroll(TopAppBarDefaults.pinnedScrollBehavior().nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (recentReadingBookIds.isNotEmpty())
            item {
                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = stringResource(R.string.continue_reading),
                        maxLines = 1,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        if (recentReadingBookIds.isNotEmpty() && recentReadingUserReadingDataMap[recentReadingBookIds.first()] != null && recentReadingBookInformationMap[recentReadingBookIds.first()] != null) {
            item {
                ReadingHeaderCard(
                    bookInformation = recentReadingBookInformationMap[recentReadingBookIds.first()]!!,
                    userReadingData = recentReadingUserReadingDataMap[recentReadingBookIds.first()]!!,
                    onClickContinueReading = onClickContinueReading
                )
            }
        }
        if (recentReadingBookIds.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 4.dp),
                        text = stringResource(
                            R.string.recent_reads, recentReadingBookIds.size,
                        ),
                        maxLines = 1,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        items(recentReadingBookIds) { id ->
            println(id)
            if (recentReadingUserReadingDataMap[id] != null && recentReadingBookInformationMap[id] != null)
                ReadingBookCard(
                    modifier = Modifier.animateItem(),
                    bookInformation = recentReadingBookInformationMap[id]!!,
                    userReadingData = recentReadingUserReadingDataMap[id]!!,
                    onClick = {
                        onClickBook(recentReadingBookInformationMap[id]!!.id)
                    }
                )
        }
        item {
            Spacer(Modifier.height(12.dp))
        }
    }
    AnimatedVisibility(
        visible = recentReadingBookIds.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        EmptyPage(
            icon = painterResource(R.drawable.empty_90dp),
            titleId = R.string.nothing_here,
            descriptionId = R.string.nothing_here_desc_reading,
            button = {
                Button(
                    onClick = onClickJumpToExploration
                ) {
                    Text(
                        text = stringResource(id = R.string.navigate_to_explore),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_reading),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        /*actions = {
            IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more)
                    )
                }
        },*/
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReadingBookCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick
                )
                .padding(4.dp),
        ) {
            Cover(
                width = 94.dp,
                height = 142.dp,
                url = bookInformation.coverUrl,
                rounded = 8.dp,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                val titleLineHeight = 20.sp
                Text(
                    modifier = Modifier
                        .height(
                            with(LocalDensity.current) { (titleLineHeight * 2.2f).toDp() }
                        )
                        .wrapContentHeight(Alignment.CenterVertically),
                    text = bookInformation.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = titleLineHeight,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bookInformation.author,
                        maxLines = 1,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 20.sp,
                        fontSize = 14.sp,
                    )
                }
                Text(
                    text = bookInformation.description.trim(),
                    maxLines = 2,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row {
                        Icon(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.CenterVertically)
                                .padding(top = 2.dp, end = 2.dp),
                            painter = painterResource(id = R.drawable.outline_schedule_24px),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = formTime(userReadingData.lastReadTime),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            fontSize = 13.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Text(
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        text = stringResource(
                            R.string.read_progress,
                            (userReadingData.readingProgress * 100).toInt().toString() + "%"
                        )
                    )
                    Text(
                        text = stringResource(R.string.read_minutes, (userReadingData.totalReadTime) / 60),
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 13.sp,
                        lineHeight = 14.sp
                    )
                }

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { userReadingData.readingProgress }
                )
            }
        }
    }
}

@Composable
private fun ReadingHeaderCard(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClickContinueReading: (Int, Int) -> Unit
) {
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(178.dp)
                .padding(4.dp),
        ) {
            Box {
                Cover(
                    height = 178.dp,
                    width = 122.dp,
                    url = bookInformation.coverUrl,
                    rounded = 8.dp
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(id = R.drawable.filled_menu_book_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(
                            R.string.last_read_info,
                            formTime(userReadingData.lastReadTime)
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                    )
                }
                val titleLineHeight = 24.sp
                Text(
                    modifier = Modifier
                        .height(
                            with(LocalDensity.current) { (titleLineHeight * 2.2f).toDp() }
                        )
                        .wrapContentHeight(Alignment.CenterVertically),
                    text = bookInformation.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.W700,
                    fontSize = 19.sp,
                    lineHeight = titleLineHeight,
                )
                Text(
                    text = userReadingData.lastReadChapterTitle,
                    maxLines = 1,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.W600
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onClickContinueReading(bookInformation.id, userReadingData.lastReadChapterId) }) {
                        Text(
                            text = stringResource(R.string.resume_last_reading),
                            fontWeight = FontWeight.W600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

        }
    }
}