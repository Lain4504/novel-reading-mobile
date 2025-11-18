package com.miraimagiclab.novelreadingapp.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.miraimagiclab.novelreadingapp.BuildConfig
import com.miraimagiclab.novelreadingapp.R
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateCheckRepository @Inject constructor(
    @param:ApplicationContext @field:ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var checkJob: Job? = null
    private val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}&hl=en_US&gl=US"

    var release: Release? = null
        private set
    private val mutableAvailable: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val availableFlow: Flow<Boolean> = mutableAvailable
    private val _updatePhase = MutableStateFlow(context.getString(R.string.update_phase_not_checked))
    val updatePhase: Flow<String> = _updatePhase

    init {
        coroutineScope.launch {
            if (userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path).getOrDefault(true)) {
                check()
            }
        }
    }

    fun resetAvailable() {
        coroutineScope.launch {
            mutableAvailable.update { false }
        }
    }

    fun check() {
        if (checkJob?.isActive == true) return
        checkJob = coroutineScope.launch {
            try {
                _updatePhase.emit(context.getString(R.string.update_phase_checking_play_store))
                val latestVersionName = fetchLatestVersionName()
                if (latestVersionName == null) {
                    release = null
                    mutableAvailable.emit(false)
                    _updatePhase.emit(context.getString(R.string.update_phase_failed))
                    return@launch
                }
                val remoteVersionCode = versionNameToCode(latestVersionName)
                if (remoteVersionCode > BuildConfig.VERSION_CODE) {
                    release = Release(
                        version = remoteVersionCode,
                        versionName = latestVersionName,
                        releaseNotes = context.getString(R.string.update_phase_play_store_new_version),
                        storeUrl = playStoreUrl
                    )
                    mutableAvailable.emit(true)
                    _updatePhase.emit(
                        context.getString(
                            R.string.update_phase_available,
                            latestVersionName
                        )
                    )
                } else {
                    release = null
                    mutableAvailable.emit(false)
                    _updatePhase.emit(context.getString(R.string.update_phase_latest))
                }
            } catch (e: Exception) {
                Log.e("UpdateChecker", "Failed to query Play Store", e)
                release = null
                mutableAvailable.emit(false)
                _updatePhase.emit(
                    context.getString(
                        R.string.update_phase_failed_with_reason,
                        e.localizedMessage ?: e.javaClass.simpleName
                    )
                )
            }
        }
    }

    fun downloadUpdate() {
        val url = release?.storeUrl ?: playStoreUrl
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun fetchLatestVersionName(): String? {
        return Jsoup.connect(playStoreUrl)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .timeout(6000)
            .get()
            .toString()
            .let { body ->
                VERSION_REGEX.find(body)?.groupValues?.get(1)?.trim()
            }
    }

    private fun versionNameToCode(versionName: String): Int {
        val numericParts = versionName.replace("[^0-9.]".toRegex(), "")
            .split(".")
        val major = numericParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = numericParts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = numericParts.getOrNull(2)?.toIntOrNull() ?: 0
        val build = numericParts.getOrNull(3)?.toIntOrNull() ?: 0
        return major * 1_000_000 + minor * 10_000 + patch * 1000 + build
    }

    companion object {
        private val VERSION_REGEX = "\"softwareVersion\"\\s*:\\s*\"([^\"]+)\"".toRegex()
    }
}
