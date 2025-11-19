package com.miraimagiclab.novelreadingapp.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


fun formTime(time: LocalDateTime): String {
    val now = LocalDateTime.now()
    val yearDiff = now.year - time.year
    val monthDiff = now.monthValue - time.monthValue
    val dayDiff = now.dayOfYear - time.dayOfYear
    val hourDiff = now.hour - time.hour
    val minuteDiff = now.minute - time.minute

    val vietnamLocale = Locale("vi", "VN")

    return when {
        time == LocalDateTime.MIN -> "Chưa bao giờ"
        yearDiff > 1 ->
            DateTimeFormatter
                .ofPattern("d MMM uuuu", vietnamLocale)
                .format(time)
        yearDiff == 1 -> "Năm ngoái"
        (dayDiff > 3 || monthDiff > 1) ->
            DateTimeFormatter
                .ofPattern("d MMM", vietnamLocale)
                .format(time)
        dayDiff in 1..3 -> {
            val prefix = when (dayDiff) {
                1 -> "Hôm qua"
                2 -> "Hôm kia"
                3 -> "Ba ngày trước"
                else -> ""
            }
            if (dayDiff <= 2) {
                val minute = time.minute.toString().padStart(2, '0')
                "$prefix ${time.hour}:$minute"
            } else {
                prefix
            }
        }
        hourDiff in 1..24 -> "$hourDiff giờ trước"
        minuteDiff in 1 until 60 -> "$minuteDiff phút trước"
        minuteDiff == 0 -> "Vừa xong"
        else -> "Rất lâu trước"
    }
}