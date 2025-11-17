package com.miraimagiclab.novelreadingapp.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_information")
data class ChapterInformationEntity(
    @PrimaryKey
    val id: String,
    val title: String
)