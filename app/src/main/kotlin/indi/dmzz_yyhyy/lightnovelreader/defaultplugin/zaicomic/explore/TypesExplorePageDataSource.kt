package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.explore

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json.DataContent
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json.TagTypeItem
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGetJsonText
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.explore.ExplorePage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

object TypesExplorePageDataSource : ExplorePageDataSource {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())
    private val explorePage = ExplorePage("分类", exploreBooksRows)

    override val title = "分类"

    override fun getExplorePage(): ExplorePage {
        if (lock) return explorePage
        lock = true
        scope.launch {
            getAllTypes().forEach { tagTypeItem ->
                exploreBooksRows.update {
                    it + ExploreBooksRow(
                        tagTypeItem.title,
                        getTagBooks(tagTypeItem.tagId)
                    )
                }
            }
        }
        return explorePage
    }

    private suspend fun getAllTypes(): List<TagTypeItem> = Jsoup
        .connect(ZaiComic.HOST + "/app/v1/comic/filter/category?source=1&channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<CateList<TagTypeItem>>>(
                it,
                object : TypeToken<DataContent<CateList<TagTypeItem>>>() {}.type
            )
        }
        .data
        .cateList

    private data class CateList<T> (@SerializedName("cateList") val cateList: List<T>)

    private suspend fun getTagBooks(tagId: Int) = Jsoup
        .connect(ZaiComic.HOST + "/app/v1/comic/filter/list?tagId=$tagId&status=0&sortType=1&page=1&size=20&channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<ComicList<DisplayTagBook>>>(
                it,
                object : TypeToken<DataContent<ComicList<DisplayTagBook>>>() {}.type
            )
        }
        .data
        .comicList
        .map { ExploreDisplayBook(it.id, it.title, "", it.cover) }

    private data class ComicList<T> (@SerializedName("comicList") val comicList: List<T>)

    private data class DisplayTagBook(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val title: String,
        @SerializedName("cover")
        val cover: String
    )
}