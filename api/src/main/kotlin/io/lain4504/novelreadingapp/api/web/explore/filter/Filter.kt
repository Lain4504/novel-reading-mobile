package io.lain4504.novelreadingapp.api.web.explore.filter

abstract class Filter {
    abstract fun getType(): FilterTypes
    abstract fun getTitle(): String
}