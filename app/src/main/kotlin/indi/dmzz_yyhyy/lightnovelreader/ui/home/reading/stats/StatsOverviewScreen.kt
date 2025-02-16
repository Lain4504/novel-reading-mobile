package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedTextLine
import indi.dmzz_yyhyy.lightnovelreader.ui.components.HeatMapCalendar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.CalendarLayoutInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core.*
import indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.rememberHeatMapCalendarState
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsOverviewScreen(
    onClickBack: () -> Unit,
    viewModel: StatsOverviewViewModel,
    onClickDaily: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val levelMap = uiState.dateLevelMap
    val statsEntityMap = uiState.dateStatsEntityMap
    val selectedDate = uiState.selectedDate
    val isLoading = uiState.isLoading

    val formatter = DateTimeFormatter.ofPattern("MM/dd")

    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    var selection by remember { mutableStateOf(LocalDate.now()) }

    val timelineDates = statsEntityMap.keys.sortedDescending().take(30)


    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = pinnedScrollBehavior,
                onClickBack = onClickBack,
                onClickSettings = {
                    showSettingsBottomSheet = true
                },
                onClickDaily = {
                    onClickDaily(selectedDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt())
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val state = rememberHeatMapCalendarState(
                        startMonth = startDate.yearMonth,
                        endMonth = endDate.yearMonth,
                        firstVisibleMonth = LocalDate.now().yearMonth,
                        firstDayOfWeek = DayOfWeek.MONDAY
                    )

                    HeatMapCalendar(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                        state = state,
                        contentPadding = PaddingValues(end = 6.dp),
                        dayContent = { day, week ->
                            val isClicked = selectedDate == day.date
                            val level = levelMap[day.date] ?: Level.Zero
                            Day(
                                selected = isClicked,
                                day = day,
                                startDate = startDate,
                                endDate = endDate,
                                week = week,
                                level = level,
                            ) { date ->
                                selection = date
                                viewModel.selectDate(date)
                            }
                        },
                        weekHeader = { WeekHeader(it) },
                        monthHeader = { MonthHeader(it, LocalDate.now()) },
                    )
                    CalendarHint(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp), viewModel)
                }
                val hmFormatter = DateTimeFormatter.ofPattern("HH:mm")
                item {
                    val todayStats = statsEntityMap[selectedDate]
                    val bookRecords = todayStats?.bookRecords ?: emptyMap()

                    val totalSeconds = bookRecords.values.sumOf { it.totalSeconds }
                    val totalDuration = Duration.ofSeconds(totalSeconds.toLong())
                    val formattedTotalTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d",
                        totalDuration.toHours(),
                        totalDuration.toMinutesPart(),
                        totalDuration.toSecondsPart()
                    )

                    val firstSeenBook = bookRecords.minByOrNull { it.value.firstSeen }
                    val lastSeenBook = bookRecords.maxByOrNull { it.value.lastSeen }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        AnimatedText(
                            text = selectedDate.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.height(6.dp))

                        val bookEntries = bookRecords.entries.toList()
                        val maxVisibleBooks = 4
                        val hiddenCount = bookEntries.size - maxVisibleBooks

                        val readingTimeDetails = bookEntries.take(maxVisibleBooks).map { (id, record) ->
                            val bookDuration = Duration.ofSeconds(record.totalSeconds.toLong())
                            val formattedBookTime = String.format(
                                Locale.getDefault(),
                                "%02d:%02d:%02d",
                                bookDuration.toHours(),
                                bookDuration.toMinutesPart(),
                                bookDuration.toSecondsPart()
                            )
                            "id=$id" to formattedBookTime
                        }.toMutableList()
                        if (hiddenCount > 0) readingTimeDetails.add("...剩下 $hiddenCount 本书" to "")


                        val sections = listOf(
                            ExpandableSection(
                                icon = painterResource(R.drawable.schedule_90dp),
                                title = "阅读总时长",
                                titleValue = formattedTotalTime
                            ) {
                                readingTimeDetails.forEach { (left, right) ->
                                    DataItem(left, right)
                                }
                            },
                            ExpandableSection(
                                icon = painterResource(R.drawable.schedule_90dp),
                                title = "时间",
                                titleValue = ""
                            ) {
                                if (todayStats?.bookRecords?.isNotEmpty() == true) {
                                    DataItem(
                                        leftText = "最早: id=${firstSeenBook?.key}",
                                        rightText = hmFormatter.format(firstSeenBook?.value?.firstSeen)
                                            ?: ""
                                    )
                                    DataItem(
                                        leftText = "最后: id=${lastSeenBook?.key ?: "无"}",
                                        rightText = hmFormatter.format(lastSeenBook?.value?.lastSeen)
                                            ?: ""
                                    )
                                }
                            },
                        )

                        MultiExpandableCard(sections = sections)
                        Spacer(Modifier.height(20.dp))
                    }
                }

                item {
                    val selectedIndex = uiState.dateReadingTimeMap.keys.indexOf(selectedDate)
                    val convertedMap: Map<String, Int> = if (selectedIndex != -1) {
                        uiState.dateReadingTimeMap.entries
                            .sortedBy { it.key }
                            .take(selectedIndex + 1)
                            .takeLast(7)
                            .associate { it.key.format(formatter) to it.value }
                    } else {
                        emptyMap()
                    }
                    println("refreshed $convertedMap")
                    Text(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        text = "近 7 日阅读时长",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W600
                    )
                    Box(modifier = Modifier.heightIn(240.dp)) {
                        Last7DaysChart(
                            modifier = Modifier
                                .height(230.dp)
                                .padding(horizontal = 14.dp),
                            data = convertedMap
                        )
                    }
                }

                item {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatsCard(
                                modifier = Modifier.weight(1f),
                                icon = painterResource(R.drawable.outline_book_24px),
                                title = "阅读会话",
                                value = "${statsEntityMap.values.sumOf { day ->
                                    day.bookRecords.values.sumOf { it.sessions }
                                }} 次"
                            )
                            Spacer(Modifier.width(16.dp))
                            val duration = Duration.ofMinutes(statsEntityMap.values.sumOf {
                                it.readingTimeCount.getTotalMinutes()
                            }.toLong())
                            StatsCard(
                                modifier = Modifier.weight(1f),
                                icon = painterResource(R.drawable.schedule_90dp),
                                title = "阅读时长",
                                value = buildAnnotatedString {
                                    append("${duration.toHours()} 时 ${duration.toMinutesPart()} 分")
                                }.toString()
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        Text(
                            text = "时间线",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                items(timelineDates) { date ->
                    statsEntityMap[date]?.takeIf { it.bookRecords.isNotEmpty() }?.let { statsEntity ->
                        TimelineDailyStats(date, statsEntity)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineDailyStats(date: LocalDate, statsEntity: ReadingStatisticsEntity) {
    if (statsEntity.bookRecords.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("MM/dd")),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(16.dp))
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(Modifier.height(12.dp))

        val totalSeconds = statsEntity.bookRecords.values.sumOf { it.totalSeconds }
        val duration = Duration.ofSeconds(totalSeconds.toLong())
        Text(
            text = buildAnnotatedString {
                append("阅读时长 ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("${duration.toHours()}时${duration.toMinutesPart()}分")
                }
            },
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))
        val displayedBooks = statsEntity.bookRecords.keys.take(3)
        Text(
            text = buildAnnotatedString {
                append("读了 ")
                displayedBooks.forEachIndexed { index, id ->
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(id.toString())
                    }
                    if (index != displayedBooks.lastIndex) append(", ")
                }
                if (statsEntity.bookRecords.keys.size > 3) {
                    append(" 等共 ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("${statsEntity.bookRecords.keys.size}")
                    }
                    append(" 本书")
                }
            },
            color = MaterialTheme.colorScheme.onSurface
        )

        statsEntity.startedBooks.forEach { bookId ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("第一次读了 ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append(bookId.toString())
                    }
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (statsEntity.favoriteBooks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            val displayedFavs = statsEntity.favoriteBooks.take(3)
            Text(
                text = buildAnnotatedString {
                    append("收藏了 ")
                    displayedFavs.forEachIndexed { index, id ->
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(id.toString())
                        }
                        if (index != displayedFavs.lastIndex) append(", ")
                    }
                    if (statsEntity.favoriteBooks.size > 3) {
                        append(" 等共 ")
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                            append("${statsEntity.favoriteBooks.size}")
                        }
                        append(" 本书")
                    }
                },
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    value: String
) {
    OutlinedCard(
        modifier = modifier.height(96.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = icon,
                    contentDescription = "",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(10.dp))
            AnimatedText(
                text = value,
                fontSize = 26.sp
            )
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
            fontSize = 15.sp,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = rightText,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


@Composable
fun MultiExpandableCard(
    sections: List<ExpandableSection>
) {
    var expandedStates by remember { mutableStateOf(List(sections.size) { false }) }

    OutlinedCard {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)) {
            sections.forEachIndexed { index, section ->
                val isExpanded = expandedStates[index]

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedStates =
                                    expandedStates.mapIndexed { i, v -> if (i == index) !v else v }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = section.icon,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = section.title,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.weight(1f))
                        AnimatedTextLine(
                            text = section.titleValue,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(20.dp)
                                .rotate(if (isExpanded) 0f else 180f)
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            section.content()
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun horizontalLine(data: Map<String, Int>): HorizontalLine {
    val fill = fill(Color(0xfffdc8c4))
    val line = rememberLineComponent(fill = fill, thickness = 2.dp)
    val labelComponent =
        rememberTextComponent(
            margins = insets(start = 6.dp),
            padding = insets(start = 8.dp, end = 8.dp, bottom = 2.dp),
            background =
            shapeComponent(fill, CorneredShape.rounded(bottomLeft = 4.dp, bottomRight = 4.dp)),
        )
    val values = data.values
    val average = if (values.isNotEmpty()) values.average() else 0.0
    println("AVG is $average")

    return HorizontalLine(
        y = { average },
        line = line,
        labelComponent = labelComponent,
        label = { "%.1f 分钟".format(average) },
        verticalLabelPosition = Position.Vertical.Bottom,
    )
}

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

@Composable
private fun Last7DaysChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    data: Map<String, Int>
) {
    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = fill(Color(0xff916cda)), thickness = 16.dp)
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis =
            HorizontalAxis.rememberBottom(
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
                valueFormatter = BottomAxisValueFormatter,
            ),
            layerPadding = { cartesianLayerPadding(scalableStart = 8.dp, scalableEnd = 8.dp) },
            decorations = listOf(horizontalLine(data)),
        ),
        modelProducer = modelProducer,
        modifier = modifier.height(224.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
fun Last7DaysChart(modifier: Modifier = Modifier, data: Map<String, Int>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.values) }
            extras { it[BottomAxisLabelKey] = data.keys.toList() }
        }
    }
    Last7DaysChart(modelProducer, modifier, data = data)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit,
    onClickSettings: () -> Unit,
    onClickDaily: () -> Unit
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
        actions = {
            IconButton(
                onClick = onClickSettings
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_settings_24px),
                    contentDescription = "settings")
            }
            IconButton(
                onClick = onClickDaily
            ) {
                Icon(
                    painter = painterResource(R.drawable.deployed_code_update_24px),
                    contentDescription = "/*FIXME*/")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun CalendarHint(
    modifier: Modifier = Modifier,
    viewModel: StatsOverviewViewModel
) {
    val threshold by viewModel.threshold.collectAsState()

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
            text = "多 ($threshold+)",
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
            .size(daySize)
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

private val daySize = 20.dp

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier.height(daySize)
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