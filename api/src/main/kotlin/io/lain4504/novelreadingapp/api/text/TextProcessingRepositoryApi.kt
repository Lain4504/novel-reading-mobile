package io.lain4504.novelreadingapp.api.text

interface TextProcessingRepositoryApi {
    fun registerProcessors(processor: TextProcessor)
}