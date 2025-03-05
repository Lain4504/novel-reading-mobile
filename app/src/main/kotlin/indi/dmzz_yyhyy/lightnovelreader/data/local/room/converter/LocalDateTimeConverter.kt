package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object LocalDateTimeConverter {
    @TypeConverter
    fun stringToDate(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.parse(dateString)
        }
    }

    @TypeConverter
    fun dateToString(date: LocalDateTime?): String? {
        return date?.toString()
    }

    /* FIXME: DO NOT MODIFY ABOVE - IT'S LEGACY */

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    @TypeConverter
    fun localDateToInt(date: LocalDate?): Int? {
        return date?.format(dateFormatter)?.toInt()
    }

    @TypeConverter
    fun intToLocalDate(dateInt: Int?): LocalDate? {
        return dateInt?.let {
            LocalDate.parse(it.toString(), dateFormatter)
        }
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HHmmss")

    @TypeConverter
    fun localTimeToInt(time: LocalTime?): Int? {
        return time?.format(timeFormatter)?.toInt()
    }

    @TypeConverter
    fun intToLocalTime(timeInt: Int?): LocalTime? {
        return timeInt?.let {
            LocalTime.parse(it.toString(), timeFormatter)
        }
    }
}