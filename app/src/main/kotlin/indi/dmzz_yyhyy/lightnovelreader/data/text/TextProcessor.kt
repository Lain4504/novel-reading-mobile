package indi.dmzz_yyhyy.lightnovelreader.data.text

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook

interface TextProcessor {
    val enabled: Boolean

    fun processText(text: String): String
    fun List<String>.process() = this.map(::processText)
    fun <T> Map<T, String>.process() = this.mapValues { (_, text) ->
        processText(text)
    }
    fun processSearchTypeNameMap(map: Map<String, String>): Map<String, String> = map.process()
    fun processSearchTipMap(map: Map<String, String>): Map<String, String> = map.process()
    fun processBookInformation(bookInformation: BookInformation): BookInformation = bookInformation.toMutable().apply {
        this.title = processText(title)
        this.subtitle = processText(subtitle)
        this.author = processText(author)
        this.description = processText(description)
        this.publishingHouse = processText(publishingHouse)
    }
    fun processBookVolumes(bookVolumes: BookVolumes): BookVolumes = bookVolumes.copy(
        volumes = bookVolumes.volumes.map { volume ->
            volume.copy(
                volumeTitle = processText(volume.volumeTitle),
                chapters = volume.chapters.map {
                    it.copy(
                        title = processText(it.title)
                    )
                }
            )
        })
    fun processChapterContent(bookId: Int, chapterContent: ChapterContent): ChapterContent = chapterContent.toMutable().apply {
        this.content = processText(content)
    }
    fun processExplorationBooksRow(explorationDisplayBook: ExplorationDisplayBook): ExplorationDisplayBook = explorationDisplayBook.copy(
        title = this.processText(explorationDisplayBook.title),
        author = this.processText(explorationDisplayBook.author),
    )
}