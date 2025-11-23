package com.miraimagiclab.novelreadingapp.ui.home.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import com.miraimagiclab.novelreadingapp.data.setting.AbstractSettingState
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope

@Stable
class SettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val checkUpdateUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path)
    val statisticsUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.Statistics.path)

    val checkUpdate by checkUpdateUserData.asState(true)
    val statistics by statisticsUserData.asState(true)
}