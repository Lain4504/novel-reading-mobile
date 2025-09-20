package io.nightfish.lightnovelreader.api.book


data class BookVolumes(
    val bookId: Int,
    val volumes: List<Volume>
) {
    companion object {
        fun empty(bookId: Int) = BookVolumes(bookId, emptyList())
    }

    fun isEmpty(): Boolean = volumes.isEmpty()
}
