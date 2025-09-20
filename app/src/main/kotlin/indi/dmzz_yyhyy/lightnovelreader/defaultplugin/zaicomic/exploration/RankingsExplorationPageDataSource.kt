package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.zaicomic.exploration

import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExplorePage
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.MutableStateFlow

object RankingsExplorationPageDataSource : ExplorePageDataSource {
    private var lock = false
    private val exploreBooksRows: MutableStateFlow<List<ExploreBooksRow>> = MutableStateFlow(emptyList())
    private val explorePage = ExplorePage("排行", exploreBooksRows)

    override val title = "排行"

    override fun getExplorePage(): ExplorePage {
        if (lock) return explorePage
        return explorePage
    }
}