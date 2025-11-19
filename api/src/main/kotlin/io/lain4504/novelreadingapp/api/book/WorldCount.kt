package io.lain4504.novelreadingapp.api.book

import androidx.annotation.StringRes

/**
 * Đối tượng dùng để lưu số từ của sách
 * Có thể thay đổi linh hoạt đơn vị hiển thị
 * Nếu cả unit và unitResId đều null thì mặc định dùng đơn vị “chữ”
 *
 * @param count giá trị thực tế
 * @param unit tên đơn vị, trong chuỗi có thể chứa {count} để thay bằng số; nếu không có {count} thì đơn vị sẽ nối sau số
 * @param unitResId resource id của tên đơn vị, hoạt động tương tự tham số unit
 */
data class WorldCount(
    val count: Int,
    val unit: String?,
    @param:StringRes val unitResId: Int?
) {
    constructor(count: Int): this(count, null, null)
    constructor(count: Int, unit: String): this(count, unit, null)
    constructor(count: Int, @StringRes unitResId: Int): this(count, null, unitResId)
}