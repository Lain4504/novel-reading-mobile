package io.lain4504.novelreadingapp.api.bookshelf

enum class BookshelfSortType(val key: String) {
    Default("default"),
    Latest("latest");
    companion object {
        fun map(key: String): BookshelfSortType = entries.first { it.key == key }
    }
}