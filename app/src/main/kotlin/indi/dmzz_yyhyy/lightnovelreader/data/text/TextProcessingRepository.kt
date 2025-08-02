package indi.dmzz_yyhyy.lightnovelreader.data.text

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextProcessingRepository @Inject constructor(
    simplifiedTraditionalProcessor: SimplifiedTraditionalProcessor,
    formatRepository: FormatRepository
) {
    private val processors = mutableListOf<TextProcessor>()

    @Suppress("MemberVisibilityCanBePrivate")
    fun registerProcessors(processor: TextProcessor) {
        if (processors.contains(processor)) return
        processors.add(processor)
    }

    private fun <T> process(t: T, block: (TextProcessor) -> ((T) -> T)): T {
        val processors = this.processors
            .filter { it.enabled }
        var result = t
        for (processor in processors) {
            result = block.invoke(processor).invoke(result)
        }
        return result
    }

    fun processText(block: () -> String): String = process(block.invoke()) { it::processText }
    fun processSearchTypeNameMap(block: () -> Map<String, String>): Map<String, String> = process(block.invoke()) { it::processSearchTypeNameMap }
    fun processSearchTipMap(block: () -> Map<String, String>): Map<String, String> = process(block.invoke()) { it::processSearchTipMap }
    fun processBookInformation(block: () -> BookInformation): BookInformation = process(block.invoke()) { it::processBookInformation }
    fun processBookVolumes(block: () -> BookVolumes): BookVolumes = process(block.invoke()) { it::processBookVolumes }
    fun processChapterContent(bookId: Int, block: () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it)
        }
    }
    suspend fun coroutineProcessChapterContent(bookId: Int, block: suspend () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it)
        }
    }
    fun processExplorationBooksRow(explorationDisplayBook: ExplorationDisplayBook): ExplorationDisplayBook = process(explorationDisplayBook) { it::processExplorationBooksRow }

    init {
        registerProcessors(simplifiedTraditionalProcessor)
        registerProcessors(formatRepository)
    }
}