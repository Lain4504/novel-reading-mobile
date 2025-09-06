package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json

import com.google.gson.annotations.SerializedName
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.MutableChapterContent

data class ComicChapterComic(
    @SerializedName("chapter_id")
    val chapterId: Int,
    @SerializedName("comic_id")
    val comicId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("page_url")
    val pageUrl: List<String>,
) {
    fun toChapterContent(lastChapterId: Int, nextChapterId: Int): ChapterContent =
        MutableChapterContent(
            id = chapterId,
            title = title,
            lastChapter = lastChapterId,
            nextChapter = nextChapterId,
            content = pageUrl.joinToString("") { "[image]$it[image]" }
        )
}