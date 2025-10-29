package io.nightfish.lightnovelreader.api.book

data class ChapterInformation(
    val id: Int,
    val title: String
): CanBeEmpty {
    override fun isEmpty(): Boolean = id == -1
}