package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
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
                onViewSelected = viewModel::setSelectedView,
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



private fun LazyListScope.dailyStatistics(uiState: StatsDetailedUiState) {
    item {
        val data = uiState.dateStatsMap[uiState.targetDateRange.first]
        DailyChart(data)
    }
}

@Composable
fun DailyChart(
    data: ReadingStatisticsEntity?
) {

}

private fun LazyListScope.weeklyStatistics(uiState: StatsDetailedUiState) {
    item {
        val data = uiState.dateStatsMap[uiState.targetDateRange.first]
        WeeklyItem(data)
    }
}

@Composable
fun WeeklyItem(
    data: ReadingStatisticsEntity?
) {

}

private fun LazyListScope.monthlyStatistics(uiState: StatsDetailedUiState) {
    item {
        MonthlySummaryChart(uiState.dateStatsMap)
    }
}

@Composable
fun MonthlySummaryChart(
    data: Map<LocalDate, ReadingStatisticsEntity>
) {

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