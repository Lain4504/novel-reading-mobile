package indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MapConverter {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<Int, Int>>() {}.type

    @TypeConverter
    fun fromString(value: String): Map<Int, Int> {
        return gson.fromJson(value, mapType) ?: emptyMap()
    }

    @TypeConverter
    fun toString(map: Map<Int, Int>): String {
        return gson.toJson(map)
    }
}