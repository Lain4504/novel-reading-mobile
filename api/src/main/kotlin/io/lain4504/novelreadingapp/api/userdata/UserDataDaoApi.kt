package io.lain4504.novelreadingapp.api.userdata

import kotlinx.coroutines.flow.Flow

interface UserDataDaoApi {
    fun update(path: String, group: String, type: String, value: String)
    fun get(path: String): String?
    fun getFlow(path: String): Flow<String?>
    fun remove(path: String)
}