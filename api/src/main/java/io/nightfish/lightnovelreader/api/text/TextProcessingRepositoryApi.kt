package io.nightfish.lightnovelreader.api.text

interface TextProcessingRepositoryApi {
    @Suppress("MemberVisibilityCanBePrivate")
    fun registerProcessors(processor: TextProcessor)
}