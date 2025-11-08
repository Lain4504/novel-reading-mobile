package io.nightfish.lightnovelreader.plugin.js.api.book

import io.nightfish.lightnovelreader.api.book.BookInformation
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class JsBookInformation(
    override val id: Int,
    override val title: String,
    override val subtitle: String,
    override val coverUri: String,
    override val author: String,
    override val description: String,
    override val tags: List<String>,
    override val publishingHouse: String,
    override val wordCount: Int,
    val lastUpdatedZonedDateTime: ZonedDateTime,
    override val isComplete: Boolean
): BookInformation {
    override val lastUpdated: LocalDateTime = LocalDateTime.ofInstant(lastUpdatedZonedDateTime.toInstant(), ZoneId.systemDefault())

    companion object {
        fun empty(id: Int): JsBookInformation {
            return JsBookInformation(id, "", "", "", "", "", emptyList(), "", -1, ZonedDateTime.now(), false)
        }
    }
}
