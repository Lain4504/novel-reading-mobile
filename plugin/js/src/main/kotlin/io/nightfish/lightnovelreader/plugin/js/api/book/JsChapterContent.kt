package io.nightfish.lightnovelreader.plugin.js.api.book

import io.nightfish.lightnovelreader.api.book.ChapterContent

data class JsChapterContent(
    override val id: Int,
    override val title: String,
    override val content: String,
    override val lastChapter: Int,
    override val nextChapter: Int
): ChapterContent