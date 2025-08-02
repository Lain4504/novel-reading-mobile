package indi.dmzz_yyhyy.lightnovelreader.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookInformationDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookRecordDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookVolumesDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.BookshelfDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ChapterContentDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.FormattingRuleDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.ReadingStatisticsDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserReadingDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfBookMetadataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookshelfEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterInformationEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.FormattingRuleEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.VolumeEntity

@Database(
    entities = [
        BookInformationEntity::class,
        VolumeEntity::class,
        ChapterInformationEntity::class,
        ChapterContentEntity::class,
        UserReadingDataEntity::class,
        UserDataEntity::class,
        BookshelfEntity::class,
        BookshelfBookMetadataEntity::class,
        ReadingStatisticsEntity::class,
        BookRecordEntity::class,
        FormattingRuleEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class LightNovelReaderDatabase : RoomDatabase() {
    abstract fun bookInformationDao(): BookInformationDao
    abstract fun bookVolumesDao(): BookVolumesDao
    abstract fun chapterContentDao(): ChapterContentDao
    abstract fun userReadingDataDao(): UserReadingDataDao
    abstract fun userDataDao(): UserDataDao
    abstract fun bookshelfDao(): BookshelfDao
    abstract fun readingStatisticsDao(): ReadingStatisticsDao
    abstract fun bookRecordDao(): BookRecordDao
    abstract fun formattingRuleDao(): FormattingRuleDao

    companion object {
        @Volatile
        private var INSTANCE: LightNovelReaderDatabase? = null

        fun getInstance(context: Context): LightNovelReaderDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LightNovelReaderDatabase::class.java,
                        "light_novel_reader_database")
                        .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, Migration_12_13)
                        .allowMainThreadQueries()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table book_information")
                db.execSQL( "create table book_information (" +
                        "id INTEGER NOT NULL," +
                        "title TEXT NOT NULL, " +
                        "cover_url TEXT NOT NULL, " +
                        "author TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "tags TEXT NOT NULL, " +
                        "publishing_house TEXT NOT NULL, " +
                        "word_count INTEGER NOT NULL," +
                        "last_update TEXT NOT NULL, " +
                        "is_complete INTEGER NOT NULL, " +
                        "PRIMARY KEY(id))" )
                db.execSQL("delete from volume")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL( "create table book_shelf (" +
                        "id INTEGER NOT NULL," +
                        "name TEXT NOT NULL, " +
                        "sort_type TEXT NOT NULL, " +
                        "auto_cache INTEGER NOT NULL, " +
                        "system_update_reminder INTEGER NOT NULL, " +
                        "all_book_ids TEXT NOT NULL, " +
                        "pinned_book_ids TEXT NOT NULL," +
                        "updated_book_ids TEXT NOT NULL, " +
                        "PRIMARY KEY(id))"
                )
                db.execSQL( "create table book_shelf_book_metadata (" +
                        "id INTEGER NOT NULL," +
                        "last_update TEXT NOT NULL, " +
                        "book_shelf_ids TEXT NOT NULL, " +
                        "PRIMARY KEY(id))"
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("alter table user_reading_data " +
                        "add read_completed_chapter_ids text default '' not null")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table book_information")
                db.execSQL( "create table book_information (" +
                        "id INTEGER NOT NULL," +
                        "title TEXT NOT NULL, " +
                        "subtitle TEXT NOT NULL, " +
                        "cover_url TEXT NOT NULL, " +
                        "author TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "tags TEXT NOT NULL, " +
                        "publishing_house TEXT NOT NULL, " +
                        "word_count INTEGER NOT NULL," +
                        "last_update TEXT NOT NULL, " +
                        "is_complete INTEGER NOT NULL, " +
                        "PRIMARY KEY(id))" )
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("drop table volume")
                db.execSQL( "create table volume (" +
                        "book_id INTEGER NOT NULL," +
                        "volume_id INTEGER NOT NULL," +
                        "volume_title TEXT NOT NULL, " +
                        "chapter_id_list TEXT NOT NULL, " +
                        "volume_index INTEGER NOT NULL, " +
                        "PRIMARY KEY(volume_id))" )
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE reading_statistics (
                    date INTEGER NOT NULL PRIMARY KEY,
                    reading_time_count BLOB NOT NULL,
                    foreground_time INTEGER NOT NULL,
                    favorite_books TEXT NOT NULL,
                    started_books TEXT NOT NULL,
                    finished_books TEXT NOT NULL)
                """)

                db.execSQL("""
                CREATE TABLE book_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date INTEGER NOT NULL,
                    book_id INTEGER NOT NULL,
                    sessions INTEGER NOT NULL,
                    total_time INTEGER NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL)
                """)
            }
        }

        private val Migration_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                CREATE TABLE formatting_rule (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    is_regex INTEGER NOT NULL,
                    match TEXT NOT NULL,
                    replacement TEXT NOT NULL,
                    is_enabled INTEGER NOT NULL)
                """)
            }
        }
    }
}