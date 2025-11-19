package io.lain4504.novelreadingapp.api.web

import android.content.Context
import android.net.Uri
import androidx.navigation.NavController
import io.lain4504.novelreadingapp.api.book.BookInformation
import io.lain4504.novelreadingapp.api.book.BookVolumes
import io.lain4504.novelreadingapp.api.book.ChapterContent
import io.lain4504.novelreadingapp.api.book.Volume
import io.lain4504.novelreadingapp.api.web.explore.ExploreExpandedPageDataSource
import io.lain4504.novelreadingapp.api.web.explore.ExplorePageDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Interface nguồn dữ liệu web của LightNovelReader, triển khai để bổ sung các nguồn truyện mới.
 * Khi được nạp, WebBookDataSource sẽ được tiêm dependencies thông qua constructor.
 * Phiên bản: 0.5.0
 */
interface WebBookDataSource {
    val id: Int

    /**
     * Được gọi khi nguồn dữ liệu được khởi chạy
     */
    fun onLoad() {}

    /**
     * Kiểm tra đồng bộ xem toàn bộ ứng dụng có đang ở trạng thái offline hay không
     */
    suspend fun isOffLine(): Boolean

    /**
     * Trạng thái offline hiện tại của ứng dụng
     */
    val offLine: Boolean

    /**
     * Dòng trạng thái offline của ứng dụng.
     * Nên là hot flow và cập nhật liên tục.
     */
    val isOffLineFlow: Flow<Boolean>

    /**
     * Danh sách id của mọi ExplorePage
     */
    val explorePageIdList: List<String>

    /**
     * Bản đồ giữa id và ExplorePageDataSource.
     * Hàm nên an toàn khi gọi trên main thread.
     */
    val explorePageDataSourceMap: Map<String, ExplorePageDataSource>

    /**
     * Bản đồ giữa id và ExploreExpandedPageDataSource cho các trang mở rộng.
     * Hàm nên an toàn khi chạy trên main thread.
     */
    val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>

    /**
     * Bản đồ giữa id loại tìm kiếm và tên hiển thị
     */
    val searchTypeMap: Map<String, String>

    /**
     * Bản đồ giữa id loại tìm kiếm và gợi ý trong ô tìm kiếm
     */
    val searchTipMap: Map<String, String>

    /**
     * Danh sách có thứ tự của các id loại tìm kiếm
     */
    val searchTypeIdList: List<String>

    /***
     * Header cần thêm khi tải ảnh minh họa (nếu cần)
     */
    val imageHeader: Map<String, String>
        get() = emptyMap()

    /**
     * Hàm đồng bộ, có thể chặn cho tới khi nhận được dữ liệu (tự xử lý retry khi mạng lỗi).
     *
     * @param id id của sách
     * @return thông tin sách đã được chuẩn hóa, hoặc BookInformation.empty()
     */
    suspend fun getBookInformation(id: String): BookInformation

    /**
     * Hàm đồng bộ, tự xử lý retry khi mất kết nối.
     *
     * @param id id của sách
     * @return danh sách tập/chương đã chuẩn hóa, hoặc BookVolumes.empty nếu không tìm thấy
     */
    suspend fun getBookVolumes(id: String): BookVolumes

    /**
     * Hàm đồng bộ, tự xử lý reconnect khi cần.
     *
     * @param chapterId id chương
     * @param bookId id sách chứa chương
     * @return nội dung chương đã chuẩn hóa, hoặc ChapterContent.empty() nếu không có
     */
    suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent

    /**
     * Thực thi tác vụ tìm kiếm.
     *
     * Trả về Flow kết quả tìm kiếm, kết thúc bằng một phần tử rỗng [BookInformation.Companion.empty]
     * để báo hiệu hoàn tất. Hàm phải an toàn khi gọi trên main thread.
     *
     * @param searchType loại tìm kiếm
     * @param keyword từ khóa
     * @return Flow kết quả tìm kiếm
     */
    fun search(searchType: String, keyword: String): Flow<List<BookInformation>>

    /**
     * Dừng toàn bộ tác vụ tìm kiếm đang chạy.
     * Hàm nên an toàn trên main thread.
     */
    fun stopAllSearch()

    /**
     * Xử lý sự kiện bấm vào tag của sách
     */
    fun progressBookTagClick(tag: String, navController: NavController) {  }

    /**
     * Lấy URI ảnh bìa tương ứng với một tập, phục vụ xuất EPUB theo tập.
     * Nếu không có, trả về null.
     *
     * @param bookId id sách
     * @param volume tập cần lấy bìa
     * @param volumeChapterContentMap map chứa toàn bộ chương trong tập, key là chapterId
     */
    fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? = null
}