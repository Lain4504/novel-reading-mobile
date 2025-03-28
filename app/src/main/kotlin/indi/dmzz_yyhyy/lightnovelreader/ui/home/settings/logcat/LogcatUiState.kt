package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry

@Stable
interface LogcatUiState {
    val logEntries: List<LogEntry>
    val isLoading: Boolean
}

class MutableLogcatUiState : LogcatUiState {
    override var logEntries: List<LogEntry> by mutableStateOf(mutableListOf())
    override var isLoading: Boolean by mutableStateOf(false)
}