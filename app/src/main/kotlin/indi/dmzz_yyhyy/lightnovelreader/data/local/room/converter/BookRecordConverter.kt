package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord

class BookRecordConverter {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<Int, BookRecord>>() {}.type

    @TypeConverter
    fun fromString(value: String): Map<Int, BookRecord> {
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun toString(map: Map<Int, BookRecord>): String {
        return gson.toJson(map)
    }
}