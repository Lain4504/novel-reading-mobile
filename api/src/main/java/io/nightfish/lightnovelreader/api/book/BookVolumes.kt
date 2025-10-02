package io.nightfish.lightnovelreader.api.book


data class BookVolumes(
    val bookId: Int,
    val volumes: List<Volume>
): CanBeEmpty {
    companion object {
        fun empty(bookId: Int) = BookVolumes(bookId, emptyList())
    }

    override fun isEmpty(): Boolean = volumes.isEmpty()
}
