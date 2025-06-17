package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.DailyBarChart
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.LastNDaysChart
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.ReadTimeStackedBarChart
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.WeeklyCountChart
import java.time.LocalDate
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailedScreen(
    targetDate: LocalDate,
    initialize: (LocalDate) -> Unit,
    viewModel: StatsDetailedViewModel = hiltViewModel(),
    onClickBack: () -> Unit
) {
    val uiState = viewModel.uiState
    uiState.selectedDate = targetDate
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val viewOptions = listOf(
        stringResource(R.string.view_daily),
        stringResource(R.string.view_weekly),
        stringResource(R.string.view_monthly)
    )

    LaunchedEffect(targetDate) {
        initialize(targetDate)
    }

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = pinnedScrollBehavior,
                onClickBack = onClickBack,
                dateRange = uiState.targetDateRange
            )
        }
    ) { paddingValues ->
        StatisticsContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            viewOptions = viewOptions,
            onViewSelected = viewModel::setSelectedView
        )
    }
}

@Composable
private fun StatisticsContent(
    uiState: StatsDetailedUiState,
    viewOptions: List<String>,
    onViewSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SingleChoiceSegmentedButtonRow {
                    viewOptions.forEachIndexed { index, label ->
                        SegmentedButton(
                            modifier = Modifier.width(90.dp),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = viewOptions.size
                            ),
                            onClick = { onViewSelected(index).let {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } },
                            selected = uiState.selectedViewIndex == index
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        val indexes = mapOf(
            0 to { dailyStatistics(uiState) },
            1 to { weeklyStatistics(uiState) },
            2 to { monthlyStatistics(uiState) }
        )

        indexes[uiState.selectedViewIndex]?.invoke()
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                style = AppTypography.titleMedium,
                fontWeight = FontWeight.W600
            )
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    style = AppTypography.titleSmall,
                    color = colorScheme.secondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colorScheme.surfaceContainerLowest
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun DailyChart(
    uiState: StatsDetailedUiState
) {
    val books = uiState.targetDateRangeRecordsMap[uiState.selectedDate]
            ?.map { it.bookId }
            .orEmpty()
    val stats = uiState.targetDateRangeStatsMap[uiState.selectedDate]
    val isActivityVisible = stats?.startedBooks?.isNotEmpty() == true
            || stats?.finishedBooks?.isNotEmpty() == true
            || stats?.favoriteBooks?.isNotEmpty() == true

    if (isActivityVisible) {
        StatsCard(
            title = stringResource(R.string.activity)
        ) {
            val startedBooks = stats.startedBooks
            val favoriteBooks = stats.favoriteBooks
            Column {
                if (startedBooks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipToBounds(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            Text(
                                text = stringResource(R.string.activity_first_read),
                                style = AppTypography.titleSmall,
                                fontWeight = FontWeight.W500
                            )
                            val displayedTitles = startedBooks.take(2).mapNotNull { bookId ->
                                uiState.bookInformationMap[bookId]?.title?.let { title ->
                                    if (title.length > 10) title.substring(0, 10) + stringResource(R.string.ellipsis) else title
                                }
                            }
                            Text(
                                text = if (displayedTitles.size == 1) displayedTitles[0]
                                else displayedTitles.joinToString(",\n") + stringResource(R.string.activity_etc),
                                style = AppTypography.labelSmall,
                                maxLines = 2,
                                color = colorScheme.secondary,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .rotate(4f)
                                .offset(y = 24.dp)
                        ) {
                            BookStack(
                                uiState = uiState,
                                books = startedBooks,
                                count = 5
                            )
                        }
                    }
                    HorizontalDivider()
                }
                if (favoriteBooks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipToBounds(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            Text(
                                text = stringResource(R.string.activity_collections),
                                style = AppTypography.titleSmall,
                                fontWeight = FontWeight.W500
                            )
                            val displayedTitles = favoriteBooks.take(2).mapNotNull { bookId ->
                                uiState.bookInformationMap[bookId]?.title?.let { title ->
                                    if (title.length > 10) title.substring(0, 10) + stringResource(R.string.ellipsis) else title
                                }
                            }
                            Text(
                                text = if (displayedTitles.size == 1) displayedTitles[0]
                                else displayedTitles.joinToString(",\n") + stringResource(R.string.activity_etc),
                                style = AppTypography.labelSmall,
                                maxLines = 2,
                                color = colorScheme.secondary,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .rotate(-3f)
                                .offset(y = 24.dp)
                        ) {
                            BookStack(
                                uiState = uiState,
                                books = favoriteBooks,
                                count = 5
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    StatsCard(
        title = stringResource(R.string.activity_reading_time)
    ) {
        Column {
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookStack(
                    uiState = uiState,
                    books = books,
                    count = 8
                )
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.n_books, books.size))
            Spacer(Modifier.height(12.dp))
            DailyBarChart(
                recordList = uiState.targetDateRangeRecordsMap[uiState.selectedDate],
                bookInformationMap = uiState.bookInformationMap
            )
        }
    }
}

@Composable
fun WeeklyItem(
    uiState: StatsDetailedUiState,
) {
    StatsCard(
        title = stringResource(R.string.reading_details)
    ) {
        Box {
            ReadTimeStackedBarChart(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(230.dp),
                bookInformationMap = uiState.bookInformationMap,
                recordMap = uiState.targetDateRangeRecordsMap,
                dateRange = uiState.targetDateRange,
            )
        }

    }

    StatsCard(
        title = stringResource(R.string.time_distribution)
    ) {
        WeeklyCountChart(
            dateRange = uiState.targetDateRange,
            statsMap = uiState.targetDateRangeStatsMap
        )
    }
}

@Composable
fun MonthlySummaryChart(
    uiState: StatsDetailedUiState
) {
    StatsCard(
        title = stringResource(R.string.activity_reading_time)
    ) {
        Box {
            LastNDaysChart(
                dateRange = uiState.targetDateRange,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(230.dp),
                statsMap = uiState.targetDateRangeStatsMap
            )
        }
    }

    StatsCard(
        title = stringResource(R.string.activity_read)
    ) {
        val (startDate, endDate) = uiState.targetDateRange

        val books = uiState.targetDateRangeRecordsMap
            .filterKeys { it in startDate..endDate }
            .values
            .flatten()
            .map { it.bookId }

        Spacer(Modifier.height(10.dp))
        BookStack(
            uiState = uiState,
            books = books,
            count = 8
        )
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.n_books, books.size))
    }
}

@Composable
fun BookStack(
    uiState: StatsDetailedUiState,
    books: List<Int>,
    count: Int,
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(end = min(books.size, count).dp * 20)
    ) {
        books.distinct().take(count).fastForEachIndexed { index, bookId ->
            val scale = 1f - (index * 0.01f).coerceAtMost(0.3f)
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .zIndex((books.size - index).toFloat())
                    .offset(x = index.dp * 20)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .align(Alignment.CenterEnd),
            ) {
                uiState.bookInformationMap[bookId]?.let {
                    Cover(
                        width = 63.dp * scale,
                        height = 90.dp * scale,
                        url = it.coverUrl,
                        rounded = 6.dp
                    )
                }
            }
        }
    }
}

private fun LazyListScope.dailyStatistics(uiState: StatsDetailedUiState) {
    item {
        DailyChart(uiState)
    }
}

private fun LazyListScope.weeklyStatistics(uiState: StatsDetailedUiState) {
    item {
        WeeklyItem(uiState)
    }
}

private fun LazyListScope.monthlyStatistics(uiState: StatsDetailedUiState) {
    item {
        MonthlySummaryChart(uiState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    dateRange: Pair<LocalDate, LocalDate>,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.detail_title),
                    style = AppTypography.titleTopBar,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AnimatedText(
                    text = if (dateRange.first == dateRange.second) dateRange.second.toString()
                    else "${dateRange.first} " + stringResource(R.string.to) + " ${dateRange.second} ",
                    style = AppTypography.titleSubTopBar,
                    color = colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}