package indi.dmzz_yyhyy.lightnovelreader.data.update

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.ketch.Ketch
import com.ketch.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCheckRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository,
    private val ketch: Ketch
) {
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var checkJob: Job? = null
    var release: Release? = null
        private set
    private val mutableAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val availableFlow: Flow<Boolean> = mutableAvailable
    private val _updatePhase = MutableStateFlow("未检查")
    val updatePhase: Flow<String> = _updatePhase

    init {
        coroutineScope.launch {
            if (userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path).getOrDefault(true))
                check()
        }
    }

    fun resetAvailable() {
        coroutineScope.launch {
            mutableAvailable.update { false }
        }
    }

    fun check() {
        if (checkJob != null && checkJob!!.isActive) return
        checkJob = coroutineScope.launch {
            val updateChannelKey = userDataRepository.stringUserData(UserDataPath.Settings.App.UpdateChannel.path).get() ?: MenuOptions.UpdateChannelOptions.Development
            val distributionPlatform = userDataRepository.stringUserData(UserDataPath.Settings.App.DistributionPlatform.path).get() ?: MenuOptions.UpdatePlatformOptions.GitHub
            Log.i("UpdateChecker", "Checking for updates from $distributionPlatform/$updateChannelKey")
            if (distributionPlatform == "AppCenter") {
                _updatePhase.update { "失败: AppCenter 平台已不受支持" }
                return@launch
            }
            _updatePhase.update { "已请求更新，等待 $distributionPlatform 应答" }
            try {
                release =
                    MenuOptions.UpdatePlatformOptions
                        .getOptionWithValue(distributionPlatform).value
                        .getOptionWithValue(updateChannelKey).value
                        .parser(_updatePhase)
            } catch (e: Exception) {
                Log.e("UpdateChecker", "failed to get release")
                e.printStackTrace()
                _updatePhase.emit("${dateFormat.format(Date())} | 失败: ${e.javaClass.simpleName}\n${e.message}")
            }
            if (release != null) {
                if (release!!.version > BuildConfig.VERSION_CODE) {
                    Log.i("UpdateChecker", "Updates available: ${release!!.versionName}")
                    _updatePhase.emit("${dateFormat.format(Date())} | 有可用更新: ${release!!.versionName}")
                } else {
                    Log.i("UpdateChecker", "App is up to date (${release!!.versionName})")
                    _updatePhase.emit("${dateFormat.format(Date())} | 已是最新 (远程: ${release!!.versionName})")
                }
            }
            mutableAvailable.emit(release != null && release!!.version > BuildConfig.VERSION_CODE)
        }
    }

    fun downloadUpdate() {
        release ?: Log.e("UpdateChecker", "Didn't find the release because release is null!").also { return }
        val cacheDir = File(context.cacheDir, "updates").also {
            if (!it.exists()) it.mkdirs()
        }
        val file = cacheDir.resolve("LightNovelReader-update.apk").also {
            if (it.exists()) it.delete()
        }
        if (release!!.downloadFileProgress == null) {
            val downloadWorkId =
                ketch.download(release!!.downloadUrl, cacheDir.path, "LightNovelReader-update.apk")
            coroutineScope.launch {
                ketch.observeDownloadById(downloadWorkId).collect {
                    when (it?.status) {
                        Status.SUCCESS -> {
                            if (file.length() == 0L) return@collect
                            Log.i("UpdateChecker", "Download success, installing")
                            installApk(file)
                        }
                        Status.FAILED -> Log.e(
                            "UpdateChecker",
                            "Failed to download update apk"
                        )
                        else -> {}
                    }
                }
            }
        } else {
            cacheDir.resolve("LightNovelReader-update-data").also {
                if (it.exists()) it.delete()
            }
            val downloadWorkId =
                ketch.download(release!!.downloadUrl, cacheDir.path, "LightNovelReader-update-data")
            coroutineScope.launch {
                ketch.observeDownloadById(downloadWorkId).collect {
                    when (it?.status) {
                        Status.SUCCESS -> {
                            if (cacheDir.resolve("LightNovelReader-update-data").length() == 0L) return@collect
                            coroutineScope.launch {
                                release!!.downloadFileProgress!!(cacheDir.resolve("LightNovelReader-update-data"), file)
                                coroutineScope.launch(Dispatchers.Main) { installApk(file) }
                            }
                        }
                        Status.FAILED -> Log.e(
                            "UpdateChecker",
                            "Failed to download update apk"
                        )
                        else -> {}
                    }
                }
            }
        }
    }
    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
