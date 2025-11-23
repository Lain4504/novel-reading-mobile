package com.miraimagiclab.novelreadingapp.data.text

import com.miraimagiclab.novelreadingapp.data.content.ContentComponentRepository
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.book.BookVolumes
import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.explore.ExploreDisplayBook
import io.lain4504.novelreadingapp.api.text.ComponentProcessor
import io.lain4504.novelreadingapp.api.text.TextProcessingRepositoryApi
import io.lain4504.novelreadingapp.api.text.TextProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextProcessingRepository @Inject constructor(
    simplifiedTraditionalProcessor: SimplifiedTraditionalProcessor,
    val contentComponentRepository: ContentComponentRepository
): TextProcessingRepositoryApi {
    private val processors = mutableListOf<TextProcessor>()

    override fun registerProcessors(processor: TextProcessor) {
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
    fun processChapterContent(bookId: String, block: () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it, ComponentProcessor(
                contentComponentRepository.serializeMap, contentComponentRepository.dataKClassMap, it.content
            ))
        }
    }
    suspend fun coroutineProcessChapterContent(bookId: String, block: suspend () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it, ComponentProcessor(
                contentComponentRepository.serializeMap, contentComponentRepository.dataKClassMap, it.content
            ))
        }
    }
    fun processExploreBooksRow(exploreDisplayBook: ExploreDisplayBook): ExploreDisplayBook = process(exploreDisplayBook) { it::processExploreBooksRow }

    init {
        registerProcessors(simplifiedTraditionalProcessor)
    }
}