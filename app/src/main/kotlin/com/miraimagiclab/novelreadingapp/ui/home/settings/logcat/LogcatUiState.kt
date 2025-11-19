package com.miraimagiclab.novelreadingapp.ui.home.settings.logcat

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.miraimagiclab.novelreadingapp.data.logging.LogEntry

@Stable
interface LogcatUiState {
    val isFileMode: Boolean
    val selectedLogFile: String
    val displayedLogEntries: List<LogEntry>
}

class MutableLogcatUiState : LogcatUiState {
    override var isFileMode: Boolean by mutableStateOf(false)
    override var selectedLogFile: String by mutableStateOf("Thời gian thực")
    override var displayedLogEntries: List<LogEntry> by mutableStateOf(emptyList())
}
