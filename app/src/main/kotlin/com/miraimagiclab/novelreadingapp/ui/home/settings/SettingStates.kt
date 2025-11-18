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
    val appLocaleKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
    val statisticsUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.Statistics.path)
    val logLevelKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path)
    val isUseProxyUserData = userDataRepository.booleanUserData(UserDataPath.Settings.Data.IsUseProxy.path)
    val enableSimplifiedTraditionalTransformUserData = userDataRepository.booleanUserData(
        UserDataPath.Reader.EnableSimplifiedTraditionalTransform.path)

    val checkUpdate by checkUpdateUserData.asState(true)
    val appLocaleKey by appLocaleKeyUserData.asState("zh-CN")
    val statistics by statisticsUserData.asState(true)
    val logLevelKey by logLevelKeyUserData.asState("none")
    val isUseProxy by isUseProxyUserData.asState(false)
    val enableSimplifiedTraditionalTransform by enableSimplifiedTraditionalTransformUserData.safeAsState(false)
}