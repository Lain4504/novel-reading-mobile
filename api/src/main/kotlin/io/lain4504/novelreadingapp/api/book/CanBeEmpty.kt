package io.lain4504.novelreadingapp.api.book

interface CanBeEmpty {
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean = !isEmpty()
}