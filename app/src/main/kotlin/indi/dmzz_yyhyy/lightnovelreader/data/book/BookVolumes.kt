package indi.dmzz_yyhyy.lightnovelreader.data.book


class BookVolumes(
    val volumes: List<Volume>
) {
    companion object {
        fun empty() = BookVolumes(emptyList())
    }

    fun isEmpty(): Boolean = volumes.isEmpty()
}
