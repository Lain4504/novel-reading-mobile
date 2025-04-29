package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope

@Stable
class SettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val checkUpdateUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path)
    val appLocaleKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
    val statisticsUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.Statistics.path)
    val darkModeKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path)
    val dynamicColorsKeyUserData = userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path)
    val updateChannelKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.App.UpdateChannel.path)
    val distributionPlatformKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.App.DistributionPlatform.path)
    val logLevelKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path)
    val enableBackgroundImageUserData = userDataRepository.booleanUserData(UserDataPath.Reader.EnableBackgroundImage.path)
    val backgroundImageDisplayModeUserData = userDataRepository.stringUserData(UserDataPath.Reader.BackgroundImageDisplayMode.path)
    val backgroundImageUriUserData = userDataRepository.uriUserData(UserDataPath.Reader.BackgroundImageUri.path)
    val backgroundDarkImageUriUserData = userDataRepository.uriUserData(UserDataPath.Reader.BackgroundDarkImageUri.path)
    val textColorUserData = userDataRepository.colorUserData(UserDataPath.Reader.TextColor.path)

    val checkUpdate by checkUpdateUserData.asState(true)
    val appLocaleKey by appLocaleKeyUserData.asState("zh-CN")
    val statistics by statisticsUserData.asState(true)
    val darkModeKey by darkModeKeyUserData.asState("FollowSystem")
    val dynamicColorsKey by dynamicColorsKeyUserData.asState(false)
    val updateChannelKey by updateChannelKeyUserData.asState("Development")
    val distributionPlatformKey by distributionPlatformKeyUserData.asState("GitHub")
    val logLevelKey by logLevelKeyUserData.asState("none")
    val enableBackgroundImage by enableBackgroundImageUserData.safeAsState(false)
    val backgroundImageDisplayMode by backgroundImageDisplayModeUserData.safeAsState("fixed")
    val backgroundImageUri by backgroundImageUriUserData.safeAsState(Uri.EMPTY)
    val backgroundDarkImageUri by backgroundDarkImageUriUserData.safeAsState(Uri.EMPTY)
    val textColor by textColorUserData.safeAsState(Color.Unspecified)
}