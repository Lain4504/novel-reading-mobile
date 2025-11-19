package io.lain4504.novelreadingapp.api.web.explore.filter

import io.lain4504.novelreadingapp.api.book.BookInformation


class WordCountFilter(onChange: () -> Unit) : SliderFilter(
    title = "Giới hạn số chữ",
    description = "Chỉ hiển thị sách có số chữ lớn hơn giá trị này, nếu bằng 0 sẽ hiển thị tất cả.",
    defaultValue = 0f,
    valueRange = 0f..200_0000f,
    steps = 9,
    onChange = onChange
), LocalFilter {
    override var enabled: Boolean
        get() = value != 0f
        set(value) { if (!value) this.value = 0f }
    override val displayValue: String
        get() = if (value == 0f) "Không giới hạn" else "${(value / 1000).toInt()}K"

    override val displayTitle: String
        get() = "Số chữ"
    override fun filter(bookInformation: BookInformation): Boolean =
        !enabled || bookInformation.wordCount.count >= value
}