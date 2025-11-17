package io.lain4504.novelreadingapp.api.content

import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponent

data class ContentData (
    val components: List<AbstractContentComponent<*>>
) {
    companion object {
        fun empty() = ContentData(emptyList())
    }
}