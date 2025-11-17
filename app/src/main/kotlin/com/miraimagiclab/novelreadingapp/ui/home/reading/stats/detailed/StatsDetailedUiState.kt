package com.miraimagiclab.novelreadingapp.ui.home.reading.stats.detailed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.miraimagiclab.novelreadingapp.data.local.room.entity.BookRecordEntity
import com.miraimagiclab.novelreadingapp.data.local.room.entity.ReadingStatisticsEntity
import io.lain4504.novelreadingapp.api.book.BookInformation
import java.time.LocalDate

interface StatsDetailedUiState {
    val targetDateRangeStatsMap: Map<LocalDate, ReadingStatisticsEntity>
    val targetDateRangeRecordsMap: Map<LocalDate, List<BookRecordEntity>>
    val targetDateRange: Pair<LocalDate, LocalDate>
    var selectedDate: LocalDate
    var selectedViewIndex: Int
    val isLoading: Boolean
    val bookInformationMap: Map<String, BookInformation>
}

class MutableStatsDetailedUiState : StatsDetailedUiState {
    override var targetDateRangeStatsMap: Map<LocalDate, ReadingStatisticsEntity> by mutableStateOf(emptyMap())
    override var targetDateRangeRecordsMap: Map<LocalDate, List<BookRecordEntity>> by mutableStateOf(emptyMap())
    override var targetDateRange: Pair<LocalDate, LocalDate> by mutableStateOf(LocalDate.now() to LocalDate.now())
    override var selectedDate: LocalDate by mutableStateOf(LocalDate.now())
    override var selectedViewIndex: Int by mutableIntStateOf(0)
    override var isLoading: Boolean by mutableStateOf(false)
    override val bookInformationMap: MutableMap<String, BookInformation> = mutableStateMapOf()
}

val StatsDetailedUiState.currentViewOption: StatsViewOption
    get() = StatsViewOption.fromIndex(selectedViewIndex)

val StatsDetailedUiState.currentDateRange: ClosedRange<LocalDate>
    get() = StatsViewOption.fromIndex(selectedViewIndex).rangeFor(selectedDate)