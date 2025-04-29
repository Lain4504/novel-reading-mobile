package indi.dmzz_yyhyy.lightnovelreader

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfSortType
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateCheckRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.work.CheckUpdateWork
import indi.dmzz_yyhyy.lightnovelreader.theme.LightNovelReaderTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LightNovelReaderApp
import indi.dmzz_yyhyy.lightnovelreader.utils.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var bookshelfRepository: BookshelfRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var updateCheckRepository: UpdateCheckRepository
    @Inject lateinit var workManager: WorkManager
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var isUsingVolumeKeyFlip = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(LogUtils(applicationContext, loggerRepository))
        var appLocale by mutableStateOf("${Locale.current.platformLocale.language}-${Locale.current.platformLocale.variant}")
        var darkMode by mutableStateOf("FollowSystem")
        var dynamicColor by mutableStateOf(false)
        var lightThemeName by mutableStateOf("light_default")
        var darkThemeName by mutableStateOf("dark_default")
        installSplashScreen()
        loggerRepository.startLogging()

        workManager.enqueueUniquePeriodicWork(
            "checkUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CheckUpdateWork>(2, TimeUnit.HOURS)
                .build()
        )
        coroutineScope.launch(Dispatchers.IO) {
            if (bookshelfRepository.getAllBookshelfIds().isEmpty())
                bookshelfRepository.crateBookShelf(
                    id = 1145140721,
                    name = "已收藏",
                    sortType = BookshelfSortType.Default,
                    autoCache = false,
                    systemUpdateReminder = false
                )
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path).getFlow().collect {
                appLocale = it ?: "${Locale.current.platformLocale.language}-${Locale.current.platformLocale.variant}"
                if (appLocale.split("-").size < 2)
                    appLocale = "${Locale.current.platformLocale.language}-${Locale.current.platformLocale.variant}"
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path).getFlow().collect {
                darkMode = it ?: "FollowSystem"
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.booleanUserData(UserDataPath.Reader.IsUsingVolumeKeyFlip.path).getFlow().collect {
                it?.let { isUsingVolumeKeyFlip = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.LightThemeName.path).getFlow().collect {
                it?.let { lightThemeName = it }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkThemeName.path).getFlow().collect {
                it?.let { darkThemeName = it }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { /* Android 13 + */
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(POST_NOTIFICATIONS), 0
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            coroutineScope.launch(Dispatchers.IO) {
                userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path).getFlow().collect {
                    dynamicColor = it ?: false
                }
            }
        }
        setContent {
            LightNovelReaderTheme(
                darkMode = darkMode,
                appLocale = appLocale,
                isDynamicColor = dynamicColor,
                lightThemeName = lightThemeName,
                darkThemeName = darkThemeName
            ) {
                LightNovelReaderApp()
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(AppEvent.KEYCODE_VOLUME_UP))
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(AppEvent.KEYCODE_VOLUME_DOWN))
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                return isUsingVolumeKeyFlip
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return isUsingVolumeKeyFlip
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}