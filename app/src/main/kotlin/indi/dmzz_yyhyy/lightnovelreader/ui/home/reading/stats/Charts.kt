package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.StorageBar
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.bar.model.StackBarData
import com.himanshoe.charty.bar.model.StorageData
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.asSolidChartColor
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd")
val predefinedColors = listOf(
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFFF44336),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFF3F51B5),
    Color(0xFFFF5722),
)

fun assignColors(
    records: List<BookRecordEntity>
): Map<Int, ChartColor> {
    return records
        .groupBy { it.bookId }
        .mapValues { (_, list) -> list.sumOf { it.totalTime } }
        .toList()
        .sortedByDescending { it.second }
        .mapIndexed { index, (bookId, _) ->
            val color = if (index < predefinedColors.size) {
                predefinedColors[index]
            } else {
                Color.Gray
            }
            bookId to color.asSolidChartColor()
        }
        .toMap()
}

@Composable
fun ReadTimeStackedBarChart(
    bookInformationMap: Map<Int, BookInformation>,
    dateRange: Pair<LocalDate, LocalDate>,
    recordMap: Map<LocalDate, List<BookRecordEntity>>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val (startDate, endDate) = dateRange

    val allRecords = recordMap
        .filterKeys { it in startDate..endDate }
        .values
        .flatten()
    val bookColors = assignColors(allRecords)

    val filteredRecordMap = recordMap
        .filterKeys { it in startDate..endDate }
        .toSortedMap()

    val data = filteredRecordMap.map { (date, records) ->
        val groupedRecords = records.groupBy { it.bookId }

        val sortedBooks = groupedRecords.entries
            .map { entry ->
                val bookTime = entry.value.sumOf { it.totalTime / 60 }.toFloat()
                entry.key to bookTime
            }
            .sortedByDescending { it.second }

        val (coloredBooks, grayBooks) = sortedBooks.partition { bookColors[it.first]?.value?.first() != Color.Gray }
        val sortedBooksWithColors = coloredBooks + grayBooks

        val values = sortedBooksWithColors.map { it.second }
        val colors = sortedBooksWithColors.map { bookColors[it.first] ?: Color.Gray.asSolidChartColor() }

        StackBarData(
            label = date.format(dateFormatter),
            values = values,
            colors = colors
        )
    }

    if (data.isEmpty()) return
    if (data.all { barData -> barData.values.all { it <= 0.0f } }) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("没有记录")
        }
        return
    }

    Column {
        StackedBarChart(
            data = { data },
            target = null,
            modifier = modifier,
            labelConfig = LabelConfig(
                showXLabel = data.size < 8,
                xAxisCharCount = 5,
                showYLabel = true,
                labelTextStyle = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W500,
                ),
                textColor = colorScheme.secondary.asSolidChartColor()
            ),
            barChartColorConfig = BarChartColorConfig.default().copy(
                barBackgroundColor = Color.Transparent.asSolidChartColor()
            ),
            onBarClick = { index, _ -> selectedIndex = index }
        )
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            val (coloredBooks, grayBooks) = bookColors.entries.partition { it.value.value.first() != Color.Gray }
            coloredBooks.fastForEach { (bookId, chartColor) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(chartColor.value.first())
                    )
                    Text(
                        text = bookInformationMap[bookId]?.title ?: "...",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (grayBooks.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Text(
                        text = "其他",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun LastNDaysChart(
    modifier: Modifier = Modifier,
    dateRange: Pair<LocalDate, LocalDate>,
    statsMap: Map<LocalDate, ReadingStatisticsEntity>
) {
    val readTimeMap = statsMap.mapValues { (_, entity) ->
        entity.readingTimeCount.getTotalMinutes()
    }

    val (startDate, endDate) = dateRange
    val filteredMap = readTimeMap.filterKeys { it in startDate..endDate }
        .toSortedMap()
    val convertedMap: Map<String, Int> = filteredMap
        .map { (date, time) -> date.format(dateFormatter) to time }
        .toMap()

    if (convertedMap.values.sum() < 1) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("没有记录")
        }
        return
    }
    val data = filteredMap.map { (date, value) ->
        BarData(
            xValue = date.format(dateFormatter),
            yValue = value.toFloat()
        )
    }
    BarChart(
        modifier = modifier,
        labelConfig = LabelConfig.default().copy(
            showXLabel = data.size < 8,
            xAxisCharCount = 5,
            showYLabel = true,
            labelTextStyle = TextStyle(
                color = colorScheme.secondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
            )
        ),
        barChartColorConfig = BarChartColorConfig.default().copy(
            fillBarColor = colorScheme.primary.copy(alpha = 0.75f).asSolidChartColor()
        ),
        data = { data },
        barChartConfig = BarChartConfig.default().copy(
            cornerRadius = CornerRadius(10f),
        )
    )
}

@Composable
fun DailyBarChart(
    recordList: List<BookRecordEntity>?,
    bookInformationMap: Map<Int, BookInformation>
) {
    if (recordList == null) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("没有记录")
        }
        return
    }

    val bookColors = assignColors(recordList)
    val totalTime = recordList.sumOf { it.totalTime }.toFloat()
    val grouped = recordList.groupBy { it.bookId }

    val data = grouped.map { (bookId, records) ->
        val bookTime = records.sumOf { it.totalTime }.toFloat()
        StorageData(
            name = bookInformationMap[bookId]?.title ?: "Unknown",
            value = if (totalTime > 0) bookTime / totalTime else 0f,
            color = bookColors[bookId] ?: Color.Gray.asSolidChartColor()
        )
    }.sortedByDescending { it.value }

    Column(modifier = Modifier.fillMaxWidth()
        .padding(vertical = 4.dp)
    ) {
        StorageBar(
            data = { data },
            modifier = Modifier.fillMaxWidth().height(30.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        ) {
            data.fastForEach {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(it.color.value.first())
                    )
                    Text(
                        text = it.name,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyCountChart(
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
    dateRange: Pair<LocalDate, LocalDate>,
) {
    val (startDate, endDate) = dateRange
    val countMap = statsMap.mapValues { (_, entity) ->
        entity.readingTimeCount
    }
    val data = countMap
        .filterKeys { it in startDate..endDate }
        .toSortedMap()
        .map { (date, count) ->
            DayData(
                dayLabel = date.format(dateFormatter),
                hourValues = List(24) { hour -> count.getMinute(hour) }
            )
        }

    if (countMap.entries.sumOf { it.value.getTotalMinutes() } < 1) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("没有记录")
        }
        return
    }

    CountChart(
        data = { data },
        modifier = Modifier
            .fillMaxSize()
            .height(320.dp)
            .padding(16.dp),
        config = CountChartConfig(
            barColor = colorScheme.onPrimaryContainer,
            daySpacing = 12.dp
        ),
        labelConfig = LabelConfig(
            showXLabel = true,
            showYLabel = true,
            textColor = Color.DarkGray.asSolidChartColor(),
            xAxisCharCount = 7,
            labelTextStyle = null,
        )
    )
}