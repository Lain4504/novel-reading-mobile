package io.nightfish.lightnovelreader.api.book

data class ChapterInformation(
    val id: Int,
    val title: String
) {
    fun isEmpty(): Boolean = id == -1
}