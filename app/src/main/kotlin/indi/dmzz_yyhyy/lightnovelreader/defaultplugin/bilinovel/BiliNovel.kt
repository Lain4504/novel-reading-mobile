package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.bilinovel

import indi.dmzz_yyhyy.lightnovelreader.utils.Cache
import indi.dmzz_yyhyy.lightnovelreader.utils.RequestLimit
import indi.dmzz_yyhyy.lightnovelreader.utils.RequestMarge
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGet
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionPost
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@WebDataSource(
    name = "BiliNovel",
    provider = "NightFish"
)
object BiliNovel: WebBookDataSource {
    private const val HOST = "https://www.bilinovel.com"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val nextPageRegex = Regex("url_next:'(.*?)'")
    private val lastPageRegex = Regex("url_previous:'(.*?)'")
    private var searchJob: Job? = null
    private val requestLimit = RequestLimit(
        maxRequestCount = 4,
        requestDelay = 250
    )

    override val id = "BiliNovel".hashCode()

    private val cache = Cache(
        timeout = 5 * 60 * 1000,
        maxCountEachType = 30
    )
    private val requestMarge = RequestMarge()

    private inline fun <reified T> ifCache(id: Int, block: () -> T): T {
        val cacheData = cache.getCache<T>(id)
        if (cacheData == null || (cacheData is CanBeEmpty && cacheData.isEmpty())) {
            val data = block.invoke()
            cache.cache(id, data)
            return data
        }
        println("cache hit!")
        return cacheData
    }

    private suspend inline fun <reified T: Any> optimize(id: Int, noinline block: suspend () -> T): T {
        return ifCache(id) {
            requestMarge.margeRequest(id, block)
        }
    }


    override suspend fun isOffLine(): Boolean =
        try {
            Jsoup
                .connect(HOST)
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "PC")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("x-requested-with", "com.z1752720130607")
                .header("sec-fetch-site", "none")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("sec-fetch-dest", "document")
                .header("accept-encoding", "gzip, deflate")
                .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("cookie:night=1; _ga=GA1.1.1542000905.1755701728; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755701728; HMACCOUNT=CCC45BA0386F2F40; __gads=ID=a36e7a5dac0092c4:T=1755705474:RT=1755705474:S=ALNI_MagzVWGVe25VxY9JZEje8HghhfYag; __gpi=UID=00000f1b79a9b010:T=1755705474:RT=1755705474:S=ALNI_MbZoAuIQZhg-folef2xv_sHTIGpFg; __eoi=ID=c71ce07f355e0272:T=1755705474:RT=1755705474", "S=AA-AfjZY2C3Y7oo9kiCuPEUYE7D3; jieqiRecentRead=4617.272882.0.1.1755705612.0; jieqiVisitId=article_articleviews%3D26%7C4617%7C2727; jieqiVisitTime=jieqiArticlesearchTime%3D1755705688; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755705764; _ga_1K4JZ603WH=GS2.1.s1755705139\$o2\$g1\$t1755705764\$j54\$l0\$h0")
                .get()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }

    override var offLine: Boolean = false
    override val isOffLineFlow: Flow<Boolean> = flow {
        while (true) {
            offLine = isOffLine()
            if (offLine) delay(500)
            else delay(4000)
        }
    }
    override val explorePageIdList: List<String> = listOf("首页")
    override val explorePageDataSourceMap: Map<String, ExplorePageDataSource> = mapOf()
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        mapOf()
    override val searchTypeMap: Map<String, String> = mapOf("name" to "按书本名称搜索")
    override val searchTipMap: Map<String, String> = mapOf("name" to "请输入书本名称")
    override val searchTypeIdList: List<String> = listOf("name")
    override val imageHeader: Map<String, String> = mapOf("referer" to "https://www.linovelib.com/")

    override suspend fun getBookInformation(id: Int): BookInformation = optimize(id) {
        val data = requestLimit.limit {
            Jsoup.connect("$HOST/novel/$id.html")
                .header("user-agent", "PC")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "PC")
                .header(
                    "accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                )
                .header("x-requested-with", "com.z1752720130607")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("sec-fetch-dest", "document")
                .header("accept-encoding", "gzip, deflate")
                .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header(
                    "cookie",
                    "night=1; _ga_1K4JZ603WH=GS2.1.s1755701727\$o1\$g0\$t1755701727\$j60\$l0\$h0; _ga=GA1.1.1542000905.1755701728; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755701728; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755701728; HMACCOUNT=CCC45BA0386F2F40"
                )
                .autoReconnectionGet(reconnectDelay = 2000)
        } ?: return@optimize BookInformation.empty(id)
        return@optimize MutableBookInformation(
            id = id,
            title = data.selectFirst("head > meta[property=og:title]")
                ?.attr("content") ?: return@optimize BookInformation.empty(id),
            subtitle = "",
            coverUrl = data.selectFirst("head > meta[property=og:image]")
                ?.attr("content") ?: return@optimize BookInformation.empty(id),
            author = data.selectFirst("head > meta[property=og:novel:author]")
                ?.attr("content") ?: return@optimize BookInformation.empty(id),
            description = data.selectFirst("head > meta[property=og:description]")
                ?.attr("content") ?: return@optimize BookInformation.empty(id),
            tags = data.select("#bookDetailWrapper > div > div.book-layout > div.book-cell > p:nth-child(5) > span > em > a")
                .map(Element::text),
            publishingHouse = data.selectFirst("head > meta[property=og:novel:category]")
                ?.attr("content") ?: return@optimize BookInformation.empty(id),
            wordCount = data.selectFirst("#bookDetailWrapper > div > div.book-layout > div.book-cell > p:nth-child(4)")
                ?.text()
                ?.split("万字")
                ?.getOrNull(0)
                ?.trim()
                ?.toFloat()
                ?.times(10000)
                ?.toInt()
                ?: return@optimize BookInformation.empty(id),
            lastUpdated = data.selectFirst("head > meta[property=og:novel:update_time]")
                ?.attr("content")
                ?.let { LocalDateTime.parse(it, dateFormatter) }
                ?: return@optimize BookInformation.empty(id),
            isComplete = data.selectFirst("head > meta[property=og:novel:status]")
                ?.attr("content")
                ?.let { it == "完结" } ?: return@optimize BookInformation.empty(id)
        )
    }

    override suspend fun getBookVolumes(id: Int): BookVolumes = optimize(id) {
        val data = requestLimit.limit {
            Jsoup.connect("$HOST/novel/$id/catalog")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "PC")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("x-requested-with", "com.z1752720130607")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("sec-fetch-dest", "document")
                .header("accept-encoding", "gzip, deflate")
                .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("cookie", "night=1; _ga=GA1.1.1542000905.1755701728; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755701728; HMACCOUNT=CCC45BA0386F2F40; jieqiVisitId=article_articleviews%3D26%7C4617; _ga_1K4JZ603WH=GS2.1.s1755705139\$o2\$g1\$t1755705156\$j43\$l0\$h0; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755705160")
                .autoReconnectionGet(reconnectDelay = 2000)
        } ?: return@optimize BookVolumes.empty(id)
        return@optimize BookVolumes(
            bookId = id,
            volumes = data.select("#volumes > div > ul").mapIndexedNotNull { index, element ->
                val volumeTitle = element.selectFirst("li.chapter-bar > h3")?.text() ?: return@mapIndexedNotNull null
                val chapters = element.select("a.chapter-li-a")
                    .mapNotNull { chapterElement ->
                        ChapterInformation(
                            id = chapterElement
                                .attr("href")
                                .split(".")
                                .getOrNull(0)
                                ?.split("/")
                                ?.lastOrNull()
                                ?.trim()
                                ?.toIntOrNull() ?: return@mapNotNull null,
                            title = chapterElement.selectFirst("a > span")
                                ?.text() ?: return@mapNotNull null
                        )
                    }
                return@mapIndexedNotNull Volume(
                    volumeId = index,
                    volumeTitle = volumeTitle,
                    chapters = chapters,
                )
            }
        )
    }

    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent = optimize(chapterId) {
        var data = requestLimit.limit { Jsoup.connect("$HOST/novel/$bookId/$chapterId.html")
            .header(":method", "GET")
            .header(":authority", "www.bilinovel.com")
            .header(":scheme", "https")
            .header("upgrade-insecure-requests", "1")
            .header("user-agent", "PC")
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .header("x-requested-with", "com.z1752720130607")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-mode", "navigate")
            .header("sec-fetch-user", "?1")
            .header("sec-fetch-dest", "document")
            .header("referer", "https://www.bilinovel.com/novel/$bookId/catalog")
            .header("accept-encoding", "gzip, deflate")
            .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("cookie", "night=1; _ga=GA1.1.1542000905.1755701728; HMACCOUNT=CCC45BA0386F2F40; jieqiRecentRead=4617.272882.0.1.1755705612.0; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1758346077; __gads=ID=a36e7a5dac0092c4:T=1755705474:RT=1758346101:S=ALNI_MagzVWGVe25VxY9JZEje8HghhfYag; __gpi=UID=00000f1b79a9b010:T=1755705474:RT=1758346101:S=ALNI_MbZoAuIQZhg-folef2xv_sHTIGpFg; __eoi=ID=c71ce07f355e0272:T=1755705474:RT=1758346101:S=AA-AfjZY2C3Y7oo9kiCuPEUYE7D3; jieqiVisitId=article_articleviews%3D3944; _ga_1K4JZ603WH=GS2.1.s1758392031\$o5\$g1\$t1758392039\$j52\$l0\$h0; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1758392039")
            .autoReconnectionGet(reconnectDelay = 1500)
        } ?: return@optimize ChapterContent.empty(chapterId)
        val title = data.selectFirst("#atitle")?.text() ?: return@optimize ChapterContent.empty(chapterId)
        var content = data.selectFirst("#acontent")?.let(::praseContent) ?: return@optimize ChapterContent.empty(chapterId)
        var nextUrl = data.toString().let(nextPageRegex::find)?.groupValues?.getOrNull(1)
        val lastUrl = data.toString().let(lastPageRegex::find)?.groupValues?.getOrNull(1)
        var nextChapterId =
            if (nextUrl?.contains("catalog") != false) -1
            else nextUrl.split(".")
                .getOrNull(0)
                ?.split("/")
                ?.lastOrNull()
                ?.split("_")
                ?.getOrNull(0)
                ?.trim()
                ?.toIntOrNull() ?: -1
        var referer = "$HOST/novel/$bookId/$chapterId.html"
        while(nextChapterId == chapterId) {
            delay(200)
            data = requestLimit.limit {
                Jsoup.connect(HOST + nextUrl)
                .header(":method", "GET")
                .header(":authority", "www.bilinovel.com")
                .header(":scheme", "https")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "PC")
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("x-requested-with", "com.z1752720130607")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("sec-fetch-dest", "document")
                .header("referer", referer)
                .header("accept-encoding", "gzip, deflate")
                .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("cookie", "night=1; _ga=GA1.1.1542000905.1755701728; HMACCOUNT=CCC45BA0386F2F40; jieqiRecentRead=4617.272882.0.1.1755705612.0; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1758346077; __gads=ID=a36e7a5dac0092c4:T=1755705474:RT=1758346101:S=ALNI_MagzVWGVe25VxY9JZEje8HghhfYag; __gpi=UID=00000f1b79a9b010:T=1755705474:RT=1758346101:S=ALNI_MbZoAuIQZhg-folef2xv_sHTIGpFg; __eoi=ID=c71ce07f355e0272:T=1755705474:RT=1758346101:S=AA-AfjZY2C3Y7oo9kiCuPEUYE7D3; jieqiVisitId=article_articleviews%3D3944; _ga_1K4JZ603WH=GS2.1.s1758392031\$o5\$g1\$t1758392039\$j52\$l0\$h0; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1758392039")
                .autoReconnectionGet(reconnectDelay = 1500)
            } ?: return@optimize ChapterContent.empty(chapterId)
            referer = "$HOST/novel/$bookId/${nextChapterId}.html"
            content += data.selectFirst("#acontent")?.let(::praseContent) ?: ""
            nextUrl = data.toString().let(nextPageRegex::find)?.groupValues?.getOrNull(1)
            nextChapterId =
                if (nextUrl?.contains("catalog") != false) -1
                else nextUrl.split(".")
                    .getOrNull(0)
                    ?.split("/")
                    ?.lastOrNull()
                    ?.split("_")
                    ?.getOrNull(0)
                    ?.trim()
                    ?.toIntOrNull() ?: -1
        }
        return@optimize MutableChapterContent(
            id = chapterId,
            title = title,
            content = content,
            lastChapter =
                if (lastUrl?.contains("catalog") != false) -1
                else lastUrl.split(".")
                    .getOrNull(0)
                    ?.split("/")
                    ?.lastOrNull()
                    ?.split("_")
                    ?.getOrNull(0)
                    ?.trim()
                    ?.toIntOrNull() ?: -1,
            nextChapter = nextChapterId
        )
    }

    fun praseContent(element: Element): String {
        return element.joinToString(separator = "") {
            when {
                it.`is`("p") -> "       " + it.text() + "\n"
                it.`is`("br") -> "\n"
                it.`is`("img") ->
                    if (it.attr("src") == "/images/sloading.svg") "[image]${it.attr("data-src")}[image]"
                    else "[image]${it.attr("src")}[image]"
                else -> ""
            }
        } + "\n"

    }

    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        searchJob?.cancel()
        return flow {
            val books = mutableListOf<BookInformation>()
            val data = requestLimit.limit {
                Jsoup.connect("$HOST/search.html")
                    .header("cache-control", "max-age=0")
                    .header("upgrade-insecure-requests", "1")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("user-agent", "PC")
                    .header("origin", "https//www.bilinovel.com")
                    .header(
                        "accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
                    )
                    .header("x-requested-with", "com.z1752720130607")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-user", "?1")
                    .header("sec-fetch-dest", "document")
                    .header("referer", "https://www.bilinovel.com/search.html")
                    .header("accept-encoding", "gzip, deflate")
                    .header("accept-language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header(
                        "cookie",
                        "night=1; _ga=GA1.1.1542000905.1755701728; Hm_lvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755701728; HMACCOUNT=CCC45BA0386F2F40; __gads=ID=a36e7a5dac0092c4:T=1755705474:RT=1755705474:S=ALNI_MagzVWGVe25VxY9JZEje8HghhfYag; __gpi=UID=00000f1b79a9b010:T=1755705474:RT=1755705474:S=ALNI_MbZoAuIQZhg-folef2xv_sHTIGpFg; __eoi=ID=c71ce07f355e0272:T=1755705474:RT=1755705474:S=AA-AfjZY2C3Y7oo9kiCuPEUYE7D3; jieqiRecentRead=4617.272882.0.1.1755705612.0; Hm_lpvt_6f9595b2c4b57f95a93aa5f575a77fb0=1755705667; jieqiVisitId=article_articleviews%3D26%7C4617%7C2727; _ga_1K4JZ603WH=GS2.1.s1755705139\$o2\$g1\$t1755705674\$j59\$l0\$h0"
                    )
                    .data("searchkey", keyword)
                    .autoReconnectionPost()
            } ?: return@flow
            data
                .select(".book-li > a")
                .mapNotNull {
                    it.attr("href")
                        .split(".")
                        .getOrNull(0)
                        ?.split("/")
                        ?.lastOrNull()
                        ?.toIntOrNull()
                }
                .forEach {
                    delay(500)
                    books.add(getBookInformation(it))
                    emit(books)
                }
        }
    }

    override fun stopAllSearch() {  }
}