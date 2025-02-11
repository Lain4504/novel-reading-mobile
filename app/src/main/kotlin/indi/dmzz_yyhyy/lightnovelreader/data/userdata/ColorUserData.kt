package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColorUserData (
    override val path: String,
    private val userDataDao: UserDataDao
) : UserData<Color>(path) {
    override fun set(value: Color) {
        userDataDao.update(path, group, "Color", value.value.toString())
    }

    override fun get(): Color? {
        return userDataDao.get(path)?.toULong().let { Color(it?: return null) }
    }

    override fun getFlow(): Flow<Color?> {
        return userDataDao.getFlow(path).map { it?.toULong() }.map { Color(it?: return@map null) }
    }
}