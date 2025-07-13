package indi.dmzz_yyhyy.lightnovelreader.data.book

class ChapterInformation(
    val id: Int,
    val title: String
) {
    fun isEmpty(): Boolean = id == -1
}