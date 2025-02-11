package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import android.net.Uri
import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UriUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<Uri>(path) {
    override fun set(value: Uri) {
        userDataDao.update(path, group, "Uri", value.toString())
    }

    override fun get(): Uri? {
        return userDataDao.get(path)?.toUri()
    }

    override fun getFlow(): Flow<Uri?> {
        return userDataDao.getFlow(path).map { it?.toUri() }
    }
}