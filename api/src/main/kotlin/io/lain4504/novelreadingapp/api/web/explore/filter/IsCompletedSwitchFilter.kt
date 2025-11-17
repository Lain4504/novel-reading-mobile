package io.lain4504.novelreadingapp.api.web.explore.filter

import io.lain4504.novelreadingapp.api.book.BookInformation

class IsCompletedSwitchFilter(
    onChange: () -> Unit
): SwitchFilter("已完结", onChange), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        !this.enabled || bookInformation.isComplete
}