package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.LocalDate

interface StatsDetailedUiState {
    val targetDateRangeStatsMap: Map<LocalDate, ReadingStatisticsEntity>
    val targetDateRangeRecordsMap: Map<LocalDate, List<BookRecordEntity>>
    val targetDateRange: Pair<LocalDate, LocalDate>
    var selectedDate: LocalDate
    val selectedViewIndex: Int
    val isLoading: Boolean
    val bookInformationMap: Map<Int, BookInformation>
}

class MutableStatsDetailedUiState : StatsDetailedUiState {
    override var targetDateRangeStatsMap by mutableStateOf(emptyMap<LocalDate, ReadingStatisticsEntity>())
    override var targetDateRangeRecordsMap: Map<LocalDate, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var targetDateRange by mutableStateOf(Pair(LocalDate.now(), LocalDate.now()))
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var selectedViewIndex by mutableIntStateOf(0)
    override var isLoading by mutableStateOf(false)
    override var bookInformationMap = mutableStateMapOf<Int, BookInformation>()
}