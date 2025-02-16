package indi.dmzz_yyhyy.lightnovelreader.ui.navigation

import kotlinx.serialization.Serializable

object Route {
    @Serializable
    object Main {
        @Serializable
        object Reading {
            @Serializable
            object Home
            @Serializable
            object Stats {
                @Serializable
                object Overview
                @Serializable
                data class Detailed(val targetDate: Int)
            }
        }
        @Serializable
        object Bookshelf {
            @Serializable
            object Home
            @Serializable
            data class Edit(
                val id: Int,
                val title: String
            )
            @Serializable
            data class DeleteBookshelfDialog(
                val bookshelfId: Int
            )
            @Serializable
            data class AddBookToBookshelfDialog(
                val selectedBookIds: List<Int>
            )
        }
        @Serializable
        object Exploration {
            @Serializable
            object Home
            @Serializable
            object Search
            @Serializable
            data class Expanded(
                val expandedPageDataSourceId: String
            )
        }
        @Serializable
        object Settings
        @Serializable
        object SourceChangeDialog
        @Serializable
        object ExportUserDataDialog
    }
    @Serializable
    object Book {
        @Serializable
        data class Detail(
            val bookId: Int
        )
        @Serializable
        data class Content (
            val bookId: Int,
            val chapterId: Int
        )
        @Serializable
        data class ExportUserDataDialog(
            val bookId: Int,
            val title: String
        )
    }
    @Serializable
    object Debug
    @Serializable
    object UpdatesAvailableDialog
    @Serializable
    data class AddBookToBookshelfDialog(
        val bookId: Int
    )
}