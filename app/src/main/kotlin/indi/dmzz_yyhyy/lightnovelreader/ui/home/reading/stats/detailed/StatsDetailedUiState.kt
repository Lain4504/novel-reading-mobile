package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.Level
import java.time.LocalDate

interface StatsDetailedUiState {
    val dateStatsMap: Map<LocalDate, ReadingStatisticsEntity>
    val targetDateRange: Pair<LocalDate, LocalDate>
    val selectedViewIndex: Int
    val isLoading: Boolean
}

class MutableStatsDetailedUiState : StatsDetailedUiState {
    override var dateStatsMap by mutableStateOf(emptyMap<LocalDate, ReadingStatisticsEntity>())
    override var targetDateRange by mutableStateOf(Pair(LocalDate.now(), LocalDate.now()))
    override var selectedViewIndex by mutableIntStateOf(0)
    override var isLoading by mutableStateOf(false)
}