package com.miraimagiclab.novelreadingapp.ui.components.calendar.core

import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class CalendarWeek(val days: List<CalendarDay>) : Serializable
