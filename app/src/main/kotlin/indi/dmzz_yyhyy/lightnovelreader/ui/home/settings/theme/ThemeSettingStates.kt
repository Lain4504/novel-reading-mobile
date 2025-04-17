package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import kotlinx.coroutines.CoroutineScope

class ThemeSettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val darkModeKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path)
    val dynamicColorsKeyUserData = userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path)
    val appLocaleKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
    val enableBackgroundImageUserData = userDataRepository.booleanUserData(UserDataPath.Reader.EnableBackgroundImage.path)
    val backgroundImageDisplayModeUserData = userDataRepository.stringUserData(UserDataPath.Reader.BackgroundImageDisplayMode.path)
    val backgroundColorUserData = userDataRepository.colorUserData(UserDataPath.Reader.BackgroundColor.path)
    val backgroundImageUriUserData = userDataRepository.uriUserData(UserDataPath.Reader.BackgroundImageUri.path)

    val darkModeKey by darkModeKeyUserData.asState("FollowSystem")
    val dynamicColorsKey by dynamicColorsKeyUserData.asState(false)
    val appLocaleKey by appLocaleKeyUserData.asState("zh-CN")
    val enableBackgroundImage by enableBackgroundImageUserData.safeAsState(false)
    val backgroundImageDisplayMode by backgroundImageDisplayModeUserData.safeAsState("fixed")
    val backgroundColor by backgroundColorUserData.safeAsState(Color.Unspecified)
    val backgroundImageUri by backgroundImageUriUserData.safeAsState(Uri.EMPTY)
}