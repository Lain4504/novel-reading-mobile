package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import java.time.LocalDate

@Dao
@TypeConverters(
    LocalDateTimeConverter::class
)
interface BookRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookRecord(record: BookRecordEntity)

    @Query("SELECT * FROM book_records WHERE date = :date")
    suspend fun getBookRecordsForDate(date: LocalDate): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE bookId = :bookId AND date = :date")
    suspend fun getBookRecordByIdAndDate(bookId: Int, date: LocalDate): BookRecordEntity?

    @Query("SELECT * FROM book_records WHERE bookId = :bookId")
    suspend fun getBookRecordsByBookId(bookId: Int): List<BookRecordEntity>

    @Delete
    suspend fun deleteBookRecord(record: BookRecordEntity)
}
