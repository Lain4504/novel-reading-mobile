package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginInjector @Inject constructor(
    @field:ApplicationContext appContext: Context,
    userDataDataDao: UserDataDao,
    userDataRepository: UserDataRepository,
    webBookDataSourceManager: WebBookDataSourceManager,
    textProcessingRepository: TextProcessingRepository,
    bookshelfRepository: BookshelfRepository,
    localBookDataSource: LocalBookDataSource,
    bookRepository: BookRepository,
) {
    val injectMap = mapOf(
        Context::class.java to appContext,
        UserDataDaoApi::class.java to userDataDataDao,
        UserDataRepositoryApi::class.java to userDataRepository,
        WebBookDataSourceManagerApi::class.java to webBookDataSourceManager,
        TextProcessingRepositoryApi::class.java to textProcessingRepository,
        LocalBookDataSourceApi::class.java to localBookDataSource,
        BookRepositoryApi::class.java to bookRepository,
        BookshelfRepositoryApi::class.java to bookshelfRepository
    )

    fun providePlugin(clazz: Class<*>): LightNovelReaderPlugin? {
        try {
            return clazz.getDeclaredField("INSTANCE").get(null) as LightNovelReaderPlugin
        }
        catch (_: NoSuchFieldException) { }
        catch (_: ClassCastException) { }
        try {
            return clazz.getDeclaredConstructor().newInstance() as LightNovelReaderPlugin
        }
        catch (_: NoSuchMethodException) { }
        catch (_: SecurityException) { }
        clazz.constructors.forEach { constructor ->
            constructor.parameterTypes.all { injectMap.keys.contains(it) }
            return constructor.newInstance(*constructor.parameterTypes.map { injectMap[it] }.toTypedArray()) as LightNovelReaderPlugin
        }
        return null
    }
}