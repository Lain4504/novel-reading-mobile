package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.ReadTimeStackedBarChart
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.LastNDaysChart
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.WeeklyCountChart
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDetailedScreen(
    targetDate: LocalDate,
    initialize: (LocalDate) -> Unit,
    viewModel: StatsDetailedViewModel = hiltViewModel(),
    onClickBack: () -> Unit
) {
    val uiState = viewModel.uiState
    println("OK DetailedScreen $targetDate")
    uiState.selectedDate = targetDate
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val viewOptions = listOf("日", "周", "月")

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
    title: String,
    subTitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
                if (subTitle != null) {
                    Text(
                        text = subTitle,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                content()
            }
        }
    }
}


@Composable
fun DailyChart(
    uiState: StatsDetailedUiState
) {
    StatsCard(
        title = "阅读时长"
    ) {
        Box {
        //  StorageChart()
        }
    }
}

@Composable
fun WeeklyItem(
    uiState: StatsDetailedUiState,
) {
    StatsCard(
        title = "近 7 日阅读详情"
    ) {
        Box {
            ReadTimeStackedBarChart(
                uiState.bookInformationMap,
                dateRange = uiState.targetDateRange,
                recordMap = uiState.targetDateRangeRecordsMap,
                modifier = Modifier.padding(10.dp).fillMaxWidth().height(230.dp),
            )
        }

    }


    StatsCard("时间分布") {
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
        title = "近 30 日阅读时长"
    ) {
        Box {
            LastNDaysChart(
                dateRange = uiState.targetDateRange,
                modifier = Modifier.padding(10.dp).fillMaxWidth().height(230.dp),
                statsMap = uiState.targetDateRangeStatsMap
            )
        }

    }

    StatsCard(
        title = "本月读过"
    ) {
        Row(
            modifier = Modifier.height(80.dp),
            horizontalArrangement = Arrangement.spacedBy((-35).dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = uiState.bookInformationMap.entries.size.toString(),
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "本",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            uiState.bookInformationMap.entries
                .sortedBy { it.key }
                .take(5)
                .forEach {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.background
                            )
                            .height(84.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Cover(
                            width = 56.dp,
                            height = 80.dp,
                            url = it.value.coverUrl,
                            rounded = 4.dp
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
                    text = "详细统计",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                AnimatedText(
                    text = if (dateRange.first == dateRange.second) dateRange.second.toString()
                        else dateRange.first.toString() + " 至 " + dateRange.second,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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