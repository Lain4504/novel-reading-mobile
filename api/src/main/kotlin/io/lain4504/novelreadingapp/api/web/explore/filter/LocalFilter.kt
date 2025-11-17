package io.lain4504.novelreadingapp.api.web.explore.filter

import io.lain4504.novelreadingapp.api.book.BookInformation

interface LocalFilter {
    fun filter(bookInformation: BookInformation): Boolean
}