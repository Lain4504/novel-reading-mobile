package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.HeatMapCalendar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarDay
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarWeek
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.displayText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.yearMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.rememberHeatMapCalendarState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOverviewScreen(
    onClickBack: () -> Unit,
    viewModel: StatsOverviewViewModel,
    onClickDetailScreen: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = pinnedScrollBehavior,
                onClickBack = onClickBack,
            )
        }
    ) { paddingValues ->
        val isLoading = uiState.isLoading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { CalendarBlock(viewModel, onSelectedDate = {viewModel.selectDate(it)}) }
                item { DailyStatsBlock(uiState, onClickDetailScreen) }
                item { TotalStatsBlock(uiState) }
            }
        }
    }
}

@Composable
private fun CalendarBlock(
    viewModel: StatsOverviewViewModel,
    onSelectedDate: (LocalDate) -> Unit,
) {
    var selection by remember { mutableStateOf(LocalDate.now()) }
    val uiState = viewModel.uiState
    val datePair = uiState.dateRange
    val state = rememberHeatMapCalendarState(
        startMonth = datePair.first.yearMonth,
        endMonth = datePair.second.yearMonth,
        firstVisibleMonth = LocalDate.now().yearMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        Text(
            text = "活动",
            fontSize = 18.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(8.dp))
        HeatMapCalendar(
            state = state,
            contentPadding = PaddingValues(end = 6.dp),
            dayContent = { day, week ->
                val isClicked = uiState.selectedDate == day.date
                val level = uiState.dateLevelMap[day.date] ?: Level.Zero
                Day(
                    selected = isClicked,
                    day = day,
                    startDate = datePair.first,
                    endDate = datePair.second,
                    week = week,
                    level = level,
                ) { date ->
                    selection = date
                    onSelectedDate(date)
                }
            },
            weekHeader = { WeekHeader(it) },
            monthHeader = { MonthHeader(it, LocalDate.now()) },
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "少",
                fontSize = 12.sp
            )
            Spacer(Modifier.width(6.dp))
            Level.entries.forEach { level ->
                LevelBox(level.color)
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "多 (${uiState.thresholds}+)",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DailyStatsBlock(
    uiState: StatsOverviewUiState,
    onClickDetailScreen: (Int) -> Unit
) {
    val selectedDate = uiState.selectedDate
    val details = uiState.selectedDateDetails
    val sections = buildExpandableSections(details!!)

    Column(modifier = Modifier.padding(horizontal = 18.dp)) {

        Row(
            modifier = Modifier.height(46.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedText(
                text = selectedDate.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.W600
            )
            Spacer(Modifier.weight(1f))
            if (sections.isNotEmpty()) {
                TextButton({
                    onClickDetailScreen(
                        selectedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
                    )
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.padding(end = 10.dp),
                            text = "详情"
                        )
                        Icon(
                            painter = painterResource(R.drawable.arrow_forward_24px), null
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        StatsColumn(
            totalDuration = details.formattedTotal,
            timeDetails = details.timeDetails,
            firstBook = details.firstBook,
            firstTime = details.firstSeenTime,
            lastBook = details.lastBook,
            lastTime = details.lastSeenTime
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatsColumn(
    totalDuration: String,
    timeDetails: List<Pair<String, String>>,
    firstBook: String?,
    firstTime: String?,
    lastBook: String?,
    lastTime: String?
) {
    val hasData = firstBook != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        StatSection(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "阅读时长",
            value = totalDuration
        ) {
            val items = if (hasData) timeDetails else emptyList()
            AnimatedStatsList(items = items) { (book, time) ->
                DataItem(book, time)
            }

            if (!hasData) {
                NoRecordNotice()
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        StatSection(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "时间范围",
            value = ""
        ) {
            val timeItems = if (hasData) buildList {
                firstBook?.let {
                    add("最早记录" to "$it ($firstTime)")
                }
                lastBook?.let {
                    add("最晚记录" to "$it ($lastTime)")
                }
            } else emptyList()

            AnimatedStatsList(items = timeItems) { (label, value) ->
                DataItem(label, value)
            }

            if (!hasData) {
                NoRecordNotice()
            }
        }
    }
}

@Composable
private fun NoRecordNotice() {
    AnimatedVisibility(
        visible = true,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Text(
            text = "没有记录",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}



@Composable
private fun <T> AnimatedStatsList(
    items: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = items,
        transitionSpec = {
            (expandVertically()).togetherWith(shrinkVertically() )
        },
        label = "AnimatedStatsList"
    ) { targetItems ->
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            targetItems.forEach { item ->
                key(item.hashCode()) {
                    itemContent(item)
                }
            }
        }
    }
}



@Composable
private fun StatSection(
    icon: Painter,
    title: String,
    value: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        content()
    }
}

@Composable
private fun DataItem(leftText: String, rightText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = leftText,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = rightText,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun TotalStatsBlock(
    uiState: StatsOverviewUiState
) {
    val lazyRowState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Text(
            text = "总统计",
            fontSize = 18.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(8.dp))
        LazyRow (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyRowState,
            flingBehavior = rememberSnapFlingBehavior(lazyRowState)
        ) {
            val totalSeconds = uiState.totalRecordEntity?.totalTime ?: 0
            val totalSessions = uiState.totalRecordEntity?.sessions ?: 0
            item {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.outline_book_24px),
                    title = "阅读会话",
                    value = totalSessions.toString(),
                    unit = "次"
                )
            }

            item {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.schedule_90dp),
                    title = "阅读时长",
                    value = "${totalSeconds / 3600}",
                    unit = "时 ${(totalSeconds % 3600) / 60} 分"
                )

            }

            item {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.schedule_90dp),
                    title = "读过",
                    value = "${uiState.bookRecordsByBookId.size}",
                    unit = "本"
                )
            }

        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    value: String,
    unit: String
) {
    Surface(
        modifier = modifier
            .height(136.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(100.dp)
            )

            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .align(Alignment.TopEnd)
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class DailyDataSections(
    val icon: Painter,
    val title: String,
    val titleValue: String,
    val content: @Composable () -> Unit
)

@Composable
private fun buildExpandableSections(
    details: DailyDateDetails
): List<DailyDataSections> {
    return listOf(
        DailyDataSections(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "阅读时长",
            titleValue = details.formattedTotal
        ) {
            details.timeDetails.forEach { (book, time) ->
                DataItem(leftText = book, rightText = time)
            }
        },
        DailyDataSections(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "时间",
            titleValue = ""
        ) {
            details.firstBook?.let {
                DataItem(
                    leftText = it,
                    rightText = "最早, ${details.firstSeenTime}"
                )
            }
            details.lastBook?.let {
                DataItem(
                    leftText = it,
                    rightText = "最晚, ${details.lastSeenTime}"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_statistics),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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

@Composable
private fun Day(
    selected: Boolean? = false,
    day: CalendarDay,
    startDate: LocalDate,
    endDate: LocalDate,
    week: CalendarWeek,
    level: Level,
    onClick: (LocalDate) -> Unit,
) {
    val weekDates = week.days.map { it.date }
    val isWeekday = (day.date.dayOfWeek.value in 1..5)


    if (day.date in startDate..endDate) {
        LevelBox(
            color = if (isWeekday) level.color else level.colorWeekends,
            selected = selected,
        ) {
            onClick(day.date)
        }
    } else if (weekDates.contains(startDate)) {
        LevelBox(
            color = Color.Transparent,
        )
    }
}

@Composable
private fun LevelBox(
    color: Color,
    selected: Boolean? = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .let { modifier ->
                if (selected == true) {
                    modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)
                } else {
                    modifier
                }
            }
            .background(color = color)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    )
}

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp),
            text = dayOfWeek.displayText(),
            fontSize = 14.sp,
        )
    }

}

@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate,
) {
    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        val title = if (month.month == Month.JANUARY) {
            month.displayText(short = true)
        } else {
            month.month.displayText()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Text(text = title, fontSize = 12.sp)
        }
    }
}