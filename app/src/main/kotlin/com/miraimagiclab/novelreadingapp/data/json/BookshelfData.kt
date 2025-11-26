package com.miraimagiclab.novelreadingapp.data.json

import com.google.gson.annotations.SerializedName

// Stub data class for backward compatibility with JSON import/export
// Bookshelf functionality has been replaced with UserNovelInteraction
@Suppress("UNUSED")
data class BookshelfData(
    @SerializedName("id")
    val id: Int = -1,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("sort_type")
    val sortType: String = "",
    @SerializedName("auto_cache")
    val autoCache: Boolean = false,
    @SerializedName("system_update_reminder")
    val systemUpdateReminder: Boolean = false,
    @SerializedName("all_book_ids")
    val allBookIds: List<String> = emptyList(),
    @SerializedName("pinned_book_ids")
    val pinnedBookIds: List<String> = emptyList(),
    @SerializedName("updated_book_ids")
    val updatedBookIds: List<String> = emptyList()
)

// Stub data class for backward compatibility
@Suppress("UNUSED")
data class BookShelfBookMetadataData(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("last_update")
    val lastUpdate: String = "",
    @SerializedName("book_shelf_ids")
    val bookShelfIds: List<Int> = emptyList()
)

