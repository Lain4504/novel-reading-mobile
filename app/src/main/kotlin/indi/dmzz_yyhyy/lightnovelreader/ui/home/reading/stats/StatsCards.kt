package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTypography
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.BookStack
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsCard
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.StatsDetailedUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.currentDateRange
import java.time.LocalDate
import kotlin.random.Random

/**
 * @return startedBooks/favoriteBooks/finishedBooks 在日期范围内的 BookId 列表
 */
private fun getBooksInRange(
    statsMap: Map<LocalDate, ReadingStatisticsEntity>,
    dateRange: ClosedRange<LocalDate>,
    selector: (ReadingStatisticsEntity) -> List<Int>
): List<Int> {
    return statsMap
        .filterKeys { it in dateRange }
        .values
        .flatMap(selector)
}

/**
 * 统计详情: 活动卡片的行
 */
@Composable
private fun BookActivitySection(
    titleResId: Int,
    bookIds: List<Int>,
    bookInfoMap: Map<Int, BookInformation>,
    uiState: StatsDetailedUiState,
    modifier: Modifier = Modifier
) {
    if (bookIds.isEmpty()) return

    val angle by remember { mutableFloatStateOf(Random.nextInt(-5, 6).toFloat()) }
    val displayedTitles = bookIds.distinct().mapNotNull { id ->
        bookInfoMap[id]?.title
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(start = 24.dp)
                .weight(1f, fill = true)
        ) {
            Text(
                text = stringResource(titleResId),
                style = AppTypography.titleMedium
            )
            val titleList = displayedTitles.take(2)
            titleList.forEach {
                Text(
                    text = it,
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                    color = colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (displayedTitles.size > titleList.size)
                Text(
                    text = stringResource(R.string.activity_etc, displayedTitles.size),
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .rotate(angle)
                .offset(y = 24.dp)
        ) {
            BookStack(
                modifier = Modifier.clipToBounds(),
                uiState = uiState,
                books = bookIds,
                count = 5
            )
        }
    }
}

/**
 * 活动卡片（适用各种时间范围）
 */
@Composable
fun ActivityStatsCard(
    uiState: StatsDetailedUiState,
    modifier: Modifier = Modifier
) {
    val dateRange = uiState.currentDateRange
    val statsMap = uiState.targetDateRangeStatsMap

    val startedBooks = getBooksInRange(statsMap, dateRange) { it.startedBooks }
    val favoriteBooks = getBooksInRange(statsMap, dateRange) { it.favoriteBooks }
    val finishedBooks = getBooksInRange(statsMap, dateRange) { it.finishedBooks }

    val hasActivity = startedBooks.isNotEmpty() || favoriteBooks.isNotEmpty() || finishedBooks.isNotEmpty()
    if (!hasActivity) return

    StatsCard(
        modifier = modifier,
        title = stringResource(R.string.activity)
    ) {
        Column {
            if (startedBooks.isNotEmpty()) {
                BookActivitySection(
                    titleResId = R.string.activity_first_read,
                    bookIds = startedBooks,
                    bookInfoMap = uiState.bookInformationMap,
                    uiState = uiState
                )
                HorizontalDivider()
            }
            if (favoriteBooks.isNotEmpty()) {
                BookActivitySection(
                    titleResId = R.string.activity_collections,
                    bookIds = favoriteBooks,
                    bookInfoMap = uiState.bookInformationMap,
                    uiState = uiState
                )
                HorizontalDivider()
            }
            /*if (finishedBooks.isNotEmpty()) {
                BookActivitySection(
                    titleResId = R.string.activity_finished,
                    bookIds = finishedBooks,
                    bookInfoMap = uiState.bookInformationMap,
                    uiState = uiState
                )
                HorizontalDivider()
            }*/
        }
    }
}