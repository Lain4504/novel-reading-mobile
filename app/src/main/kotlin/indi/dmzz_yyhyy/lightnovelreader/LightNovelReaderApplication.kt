package indi.dmzz_yyhyy.lightnovelreader

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LoggerRepository
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginManager
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LightNovelReaderApplication : Application(), Configuration.Provider {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var loggerRepository: LoggerRepository
    @Inject lateinit var userDataRepository: UserDataRepository
    @Inject lateinit var pluginManager: PluginManager

    override val workManagerConfiguration: Configuration
        get()  =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        pluginManager.loadAllPlugins()
        println("loadPlugin")
        coroutineScope.launch(Dispatchers.IO) {
            loggerRepository.logLevel = LogLevel.from(userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path).getOrDefault("none"))
            loggerRepository.startLogging()
        }
        coroutineScope.launch(Dispatchers.IO) {
            ProxyPool.enable = userDataRepository.booleanUserData(UserDataPath.Settings.Data.IsUseProxy.path).getOrDefault(false)
        }
        WorkManager.getInstance(this).cancelAllWork()
    }
}