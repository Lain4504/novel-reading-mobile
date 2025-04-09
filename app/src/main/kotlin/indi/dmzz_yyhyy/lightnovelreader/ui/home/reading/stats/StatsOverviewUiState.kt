package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import java.time.LocalDate

interface StatsOverviewUiState {
    var isLoading: Boolean
    var selected: Boolean
    var selectedDate: LocalDate
    val dateRange: Pair<LocalDate, LocalDate>
    val thresholds: Int
    var dateLevelMap: Map<LocalDate, Level>
    var dateReadingTimeMap: Map<LocalDate, Int>
    var dateStatsEntityMap: Map<LocalDate, ReadingStatisticsEntity>
    var totalRecordEntity: BookRecordEntity?
    var bookRecordsByBookId: Map<Int, List<BookRecordEntity>>
    var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>>
    val bookInformationMap: Map<Int, BookInformation>
}

class MutableStatisticsOverviewUiState : StatsOverviewUiState {
    override var isLoading: Boolean by mutableStateOf(false)
    override var selected: Boolean by mutableStateOf(false)
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var dateRange: Pair<LocalDate, LocalDate> by mutableStateOf(Pair(LocalDate.now(), LocalDate.now()))
    override var thresholds: Int by mutableIntStateOf(0)
    override var dateLevelMap: Map<LocalDate, Level> by mutableStateOf(emptyMap())
    override var dateReadingTimeMap: Map<LocalDate, Int> by mutableStateOf(emptyMap())
    override var dateStatsEntityMap: Map<LocalDate, ReadingStatisticsEntity> by mutableStateOf(emptyMap())
    override var totalRecordEntity: BookRecordEntity? by mutableStateOf(null)
    override var bookRecordsByBookId: Map<Int, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var bookRecordsByDate: Map<LocalDate, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var bookInformationMap = mutableStateMapOf<Int, BookInformation>()
}
