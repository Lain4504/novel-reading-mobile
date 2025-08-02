package indi.dmzz_yyhyy.lightnovelreader.data.book

data class ChapterInformation(
    val id: Int,
    val title: String
) {
    fun isEmpty(): Boolean = id == -1
}