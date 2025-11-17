package com.miraimagiclab.novelreadingapp.data.userdata

import com.miraimagiclab.novelreadingapp.data.json.AppUserDataContent
import com.miraimagiclab.novelreadingapp.data.local.room.dao.UserDataDao
import io.lain4504.novelreadingapp.api.userdata.BooleanUserData
import io.lain4504.novelreadingapp.api.userdata.ColorUserData
import io.lain4504.novelreadingapp.api.userdata.FloatUserData
import io.lain4504.novelreadingapp.api.userdata.IntListUserData
import io.lain4504.novelreadingapp.api.userdata.IntUserData
import io.lain4504.novelreadingapp.api.userdata.StringListUserData
import io.lain4504.novelreadingapp.api.userdata.StringUserData
import io.lain4504.novelreadingapp.api.userdata.UriUserData
import io.lain4504.novelreadingapp.api.userdata.UserDataRepositoryApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepository @Inject constructor(
    private val userDataDao: UserDataDao
): UserDataRepositoryApi {
    override fun stringUserData(path: String) = StringUserData(path, userDataDao)
    override fun floatUserData(path: String) = FloatUserData(path, userDataDao)
    override fun intUserData(path: String) = IntUserData(path, userDataDao)
    override fun booleanUserData(path: String) = BooleanUserData(path, userDataDao)
    override fun intListUserData(path: String) = IntListUserData(path, userDataDao)
    override fun stringListUserData(path: String) = StringListUserData(path, userDataDao)
    override fun colorUserData(path: String) = ColorUserData(path, userDataDao)
    override fun uriUserData(path: String) = UriUserData(path, userDataDao)

    override fun remove(path: String) = userDataDao.remove(path)

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
}