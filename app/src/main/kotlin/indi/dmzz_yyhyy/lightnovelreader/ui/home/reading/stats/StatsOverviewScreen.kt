package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.HeatMapCalendar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.CalendarLayoutInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarDay
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.CalendarWeek
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.displayText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.yearMonth
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.rememberHeatMapCalendarState
import indi.dmzz_yyhyy.lightnovelreader.utils.DurationFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOverviewScreen(
    onClickBack: () -> Unit,
    viewModel: StatsOverviewViewModel,
    onClickDetailScreen: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val selectedDate = uiState.selectedDate
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
                item { DailyStatsBlock(uiState, selectedDate, onClickDetailScreen) }
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

    HeatMapCalendar(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
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
    CalendarHint(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp), thresholds = uiState.thresholds)
}

@Composable
private fun DailyStatsBlock(
    uiState: StatsOverviewUiState,
    selectedDate: LocalDate,
    onClickDetailScreen: (Int) -> Unit
) {
    val records = uiState.bookRecordsByDate[selectedDate] ?: emptyList()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
        val sections = buildExpandableSections(uiState, records, dateFormatter)

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

        if (sections.isNotEmpty()) {
            MultiExpandableCard(sections)
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "没有记录。",
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
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
                    value = if (totalSeconds < 3600) "< 1" else "${(totalSeconds / 3600)} +",
                    unit = "小时"
                )
            }

            item {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.schedule_90dp),
                    title = "读过的书本",
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

data class ExpandableSection(
    val icon: Painter,
    val title: String,
    val titleValue: String,
    val content: @Composable () -> Unit
)

@Composable
fun DataItem(leftText: String, rightText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leftText,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = rightText,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun buildExpandableSections(
    uiState: StatsOverviewUiState,
    records: List<BookRecordEntity> = emptyList(),
    dateFormatter: DateTimeFormatter
): List<ExpandableSection> {
    if (records.isEmpty()) return emptyList()
    val totalSeconds = records.sumOf { it.totalTime }
    val totalDuration = totalSeconds.toDuration(DurationUnit.SECONDS)

    val formattedTotal = DurationFormat().format(totalDuration, smallestUnit = DurationFormat.Unit.SECOND)

    val timeDetails = records.take(4).map { record ->
        val book = uiState.bookInformationMap[record.bookId]?.title ?: "Unknown"
        val duration = record.totalTime.toDuration(DurationUnit.SECONDS)

        val formattedTime = DurationFormat(Locale.ENGLISH).format(duration)
        book to formattedTime
    }.toMutableList().apply {
        if (records.size > 4) add("...${records.size - 4} more" to "")
    }

    val firstBook = records.minByOrNull { it.firstSeen }?.let {
        uiState.bookInformationMap[it.bookId]?.title
    }
    val lastBook = records.maxByOrNull { it.lastSeen }?.let {
        uiState.bookInformationMap[it.bookId]?.title
    }

    return listOf(
        ExpandableSection(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "阅读时长",
            titleValue = formattedTotal
        ) {
            timeDetails.forEach { (book, time) ->
                DataItem(leftText = book, rightText = time)
            }
        },
        ExpandableSection(
            icon = painterResource(R.drawable.schedule_90dp),
            title = "时间",
            titleValue = ""
        ) {
            if (records.isNotEmpty()) {
                DataItem(
                    leftText = firstBook ?: "unknown",
                    rightText = "最早, " + dateFormatter.format(records.minByOrNull { it.firstSeen }!!.firstSeen)
                )
                DataItem(
                    leftText = lastBook ?: "unknown",
                    rightText = "最晚, " + dateFormatter.format(records.maxByOrNull { it.lastSeen }!!.lastSeen)
                )
            }
        }
    )
}

@Composable
fun MultiExpandableCard(
    sections: List<ExpandableSection>
) {
    var expandedStates by remember { mutableStateOf(List(sections.size) { false }) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp)) {
            sections.forEachIndexed { index, section ->
                val isExpanded = expandedStates[index]

                Column(modifier = Modifier.animateContentSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                expandedStates =
                                    expandedStates.mapIndexed { u, v -> u == index != v }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = section.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = section.titleValue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (isExpanded) 0f else 180f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                                section.content()
                            }
                        }
                    }
                }
            }
        }
    }
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
private fun CalendarHint(
    modifier: Modifier = Modifier,
    thresholds: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = "少",
            fontSize = 12.sp
        )
        Level.entries.forEach { level ->
            LevelBox(level.color)
        }
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = "多 ($thresholds+)",
            fontSize = 12.sp
        )
    }
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

private fun getMonthWithYear(
    layoutInfo: CalendarLayoutInfo,
    daySize: Dp,
    density: Density,
): YearMonth? {
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month.yearMonth
        else -> {
            val firstItem = visibleItemsInfo.first()
            val daySizePx = with(density) { daySize.toPx() }
            if (
                firstItem.size < daySizePx * 4 ||
                firstItem.offset < layoutInfo.viewportStartOffset &&
                (layoutInfo.viewportStartOffset - firstItem.offset > daySizePx)
            ) {
                visibleItemsInfo[1].month.yearMonth
            } else {
                firstItem.month.yearMonth
            }
        }
    }
}