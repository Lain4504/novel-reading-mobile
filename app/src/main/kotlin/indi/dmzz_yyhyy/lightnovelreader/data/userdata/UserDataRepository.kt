package indi.dmzz_yyhyy.lightnovelreader.data.userdata

import indi.dmzz_yyhyy.lightnovelreader.data.json.AppUserDataContent
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val userDataDao: UserDataDao
) {
    fun stringUserData(path: String) = StringUserData(path, userDataDao)
    fun floatUserData(path: String) = FloatUserData(path, userDataDao)
    fun intUserData(path: String) = IntUserData(path, userDataDao)
    fun booleanUserData(path: String) = BooleanUserData(path, userDataDao)
    fun intListUserData(path: String) = IntListUserData(path, userDataDao)
    fun stringListUserData(path: String) = StringListUserData(path, userDataDao)
    fun colorUserData(path: String) = ColorUserData(path, userDataDao)
    fun uriUserData(path: String) = UriUserData(path, userDataDao)

    fun importUserData(data: AppUserDataContent): Boolean {
        val userDataList = data.userData ?: return false
        userDataList.forEach {
            userDataDao.update(
                path = it.path,
                group = it.group,
                type = it.type,
                value = it.value
            )
        }
        return true
    }

    fun remove(path: String) = userDataDao.remove(path)
}