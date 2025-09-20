package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.explore

import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json.DataContent
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json.UpdatePageItem
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

object UpdateExplorePageDataSource : ExplorePageDataSource {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())
    private val explorePage = ExplorePage("更新", exploreBooksRows)

    override val title = "更新"

    override fun getExplorePage(): ExplorePage {
        if (lock) return explorePage
        lock = true
        scope.launch {
            exploreBooksRows.update { exploreBooksRowList ->
                exploreBooksRowList + ExploreBooksRow(
                    title = "全部漫画",
                    bookList = getUpdateBooks(100)
                )
            }
        }
        scope.launch {
            exploreBooksRows.update { exploreBooksRowList ->
                exploreBooksRowList + ExploreBooksRow(
                    title = "原创漫画",
                    bookList = getUpdateBooks(1)
                )
            }
        }
        scope.launch {
            exploreBooksRows.update { exploreBooksRowList ->
                exploreBooksRowList + ExploreBooksRow(
                    title = "译制漫画",
                    bookList = getUpdateBooks(0)
                )
            }
        }
        return explorePage
    }

    private suspend fun getUpdateBooks(channel: Int) = Jsoup
        .connect(ZaiComic.HOST + "/app/v1/comic/update/list/$channel/1?channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
        .autoReconnectionGetJsonText()
        .let {
            ZaiComic.gson.fromJson<DataContent<List<UpdatePageItem>>>(
                it,
                object : TypeToken<DataContent<List<UpdatePageItem>>>() {}.type
            )
        }
        .data
        .map {
            ExploreDisplayBook(it.id, it.title, "", it.cover)
        }
}
