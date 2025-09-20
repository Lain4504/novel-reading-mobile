package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.exploration

import com.google.gson.reflect.TypeToken
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.ZaiComic
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.json.RecommendData
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.getImageSize
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

object RecommendExplorationPageDataSource : ExplorePageDataSource {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())
    private val explorePage = ExplorePage("推荐", exploreBooksRows)

    override val title = "推荐"
    override fun getExplorePage(): ExplorePage {
        if (lock) return explorePage
        lock = true
        scope.launch {
            Jsoup
                .connect(ZaiComic.HOST + "/app/v1/comic/recommend/index?channel=android&timestamp=${(System.currentTimeMillis() / 1000)}")
                .autoReconnectionGetJsonText()
                .let {
                    ZaiComic.gson.fromJson<List<RecommendData>>(
                        it,
                        object : TypeToken<List<RecommendData>>() {}.type
                    )
                }
                .forEach { recommendData ->
                    val books = recommendData.data.mapNotNull {
                        if (it.type != 1) return@mapNotNull null
                        val coverSize = getImageSize(it.cover) ?: return@mapNotNull null
                        if (coverSize.width > coverSize.height ) {
                            val bookInformation = ZaiComic.getBookInformation(it.id)
                            return@mapNotNull ExploreDisplayBook(
                                it.id,
                                it.title,
                                "",
                                bookInformation.coverUrl
                            )
                        }
                        ExploreDisplayBook(it.id, it.title, "", it.cover)
                    }
                    if (books.isEmpty()) return@forEach
                    exploreBooksRows.update {
                        it + ExploreBooksRow(recommendData.title, books)
                    }
                }
        }
        return explorePage
    }
}