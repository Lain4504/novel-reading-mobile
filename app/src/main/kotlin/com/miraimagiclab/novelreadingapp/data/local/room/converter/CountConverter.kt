package com.miraimagiclab.novelreadingapp.data.local.room.converter

import androidx.room.TypeConverter
import com.miraimagiclab.novelreadingapp.data.statistics.Count

class CountConverter {
    @TypeConverter
    fun fromCount(count: Count) = count.toByteArray()

    @TypeConverter
    fun toCount(bytes: ByteArray) = Count.Companion.fromByteArray(bytes)
}