package io.nightfish.lightnovelreader.api.book

data class Volume(
    val volumeId: Int,
    val volumeTitle: String,
    val chapters: List<ChapterInformation>,
): CanBeEmpty {
    override fun isEmpty(): Boolean = volumeId == -1
}
