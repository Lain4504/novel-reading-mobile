package com.miraimagiclab.novelreadingapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.miraimagiclab.novelreadingapp.data.local.room.converter.JsonObjectConverter
import kotlinx.serialization.json.JsonObject

@TypeConverters(
    JsonObjectConverter::class
)
@Entity(tableName = "chapter_content")
data class ChapterContentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: JsonObject,
    val lastChapter: String,
    val nextChapter: String
)
