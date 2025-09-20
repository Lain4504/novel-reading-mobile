package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter.stringListToString
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import java.time.LocalDateTime

@Dao
interface BookInformationDao {
    @TypeConverters(LocalDateTimeConverter::class)
    @Query("replace into book_information (id, title, subtitle, cover_url, author, description, tags, publishing_house, word_count, last_update, is_complete) " +
            "values (:id, :title, :subtitle, :coverUrl, :author, :description, :tags, :publishingHouse, :wordCount, :lastUpdated, :isComplete) ")
    fun update(id: Int,
               title: String,
               subtitle: String,
               coverUrl: String,
               author: String,
               description: String,
               tags: String,
               publishingHouse: String,
               wordCount: Int,
               lastUpdated: LocalDateTime,
               isComplete: Boolean)

    @Transaction
    fun update(information: BookInformation) {
        return update(
            information.id,
            information.title,
            information.subtitle,
            information.coverUrl,
            information.author,
            information.description,
            stringListToString(information.tags),
            information.publishingHouse,
            information.wordCount,
            information.lastUpdated,
            information.isComplete,
        )
    }

    @Query("select * from book_information where id=:id")
    suspend fun getEntity(id: Int): BookInformationEntity?

    @Transaction
    suspend fun get(id: Int): BookInformation? {
        val entity = getEntity(id) ?: return null
        return MutableBookInformation(
            entity.id,
            entity.title,
            entity.subtitle,
            entity.coverUrl,
            entity.author,
            entity.description,
            entity.tags,
            entity.publishingHouse,
            entity.wordCount,
            entity.lastUpdated,
            entity.isComplete,
        )
    }

    @Transaction
    suspend fun has(id: Int): Boolean {
        return get(id) != null
    }

    @Query("delete from book_information")
    fun clear()
}