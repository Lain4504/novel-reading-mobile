package com.miraimagiclab.novelreadingapp.data.update

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.lain4504.novelreadingapp.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý In-App Updates sử dụng Play Core API với Flexible Update mode.
 * 
 * Flexible Update cho phép:
 * - Download update trong background
 * - User có thể tiếp tục sử dụng app
 * - Hiển thị snackbar khi download xong
 * - User chọn restart để apply update
 */
@Singleton
class InAppUpdateRepository @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val userDataRepository: UserDataRepository
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable: Flow<Boolean> = _updateAvailable
    
    private val _updateDownloaded = MutableStateFlow(false)
    val updateDownloaded: Flow<Boolean> = _updateDownloaded
    
    private val _installState = MutableStateFlow<InstallState?>(null)
    val installState: Flow<InstallState?> = _installState
    
    private var installStateListener: InstallStateUpdatedListener? = null
    
    /**
     * Kiểm tra xem có update available không.
     * Chỉ check nếu user đã bật auto-check update.
     */
    fun checkForUpdate(callback: (AppUpdateInfo?) -> Unit) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isFlexibleUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                
                _updateAvailable.value = isUpdateAvailable && isFlexibleUpdateAllowed
                
                if (isUpdateAvailable && isFlexibleUpdateAllowed) {
                    Log.i("InAppUpdate", "Update available: ${appUpdateInfo.availableVersionCode()}")
                    callback(appUpdateInfo)
                } else {
                    Log.d("InAppUpdate", "No update available or flexible update not allowed")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("InAppUpdate", "Failed to check for update", e)
                callback(null)
            }
    }
    
    /**
     * Bắt đầu flexible update flow.
     * Update sẽ được download trong background.
     */
    fun startFlexibleUpdate(activity: Activity) {
        try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo
            appUpdateInfo.addOnSuccessListener { updateInfo ->
                if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    // Register listener để theo dõi install state
                    registerInstallStateListener()
                    
                    // Start flexible update
                    appUpdateManager.startUpdateFlowForResult(
                        updateInfo,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        REQUEST_CODE_UPDATE
                    )
                    Log.i("InAppUpdate", "Update flow started")
                } else {
                    Log.d("InAppUpdate", "No update available or flexible update not allowed")
                }
            }.addOnFailureListener { e ->
                Log.e("InAppUpdate", "Failed to start flexible update", e)
            }
        } catch (e: Exception) {
            Log.e("InAppUpdate", "Failed to start flexible update", e)
        }
    }
    
    /**
     * Đăng ký listener để theo dõi install state.
     * Listener sẽ được gọi khi:
     * - Download progress thay đổi
     * - Download hoàn thành
     * - Install state thay đổi
     */
    private fun registerInstallStateListener() {
        if (installStateListener != null) {
            return // Đã đăng ký rồi
        }
        
        installStateListener = InstallStateUpdatedListener { state ->
            _installState.value = state
            
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.i("InAppUpdate", "Update downloaded successfully")
                    _updateDownloaded.value = true
                }
                InstallStatus.INSTALLED -> {
                    Log.i("InAppUpdate", "Update installed")
                    unregisterInstallStateListener()
                }
                InstallStatus.FAILED -> {
                    Log.e("InAppUpdate", "Update failed: ${state.installErrorCode()}")
                    unregisterInstallStateListener()
                }
                InstallStatus.CANCELED -> {
                    Log.i("InAppUpdate", "Update canceled")
                    unregisterInstallStateListener()
                }
                else -> {
                    // Downloading, pending, etc.
                    Log.d("InAppUpdate", "Install status: ${state.installStatus()}")
                }
            }
        }
        
        appUpdateManager.registerListener(installStateListener!!)
    }
    
    /**
     * Hủy đăng ký listener.
     */
    private fun unregisterInstallStateListener() {
        installStateListener?.let {
            appUpdateManager.unregisterListener(it)
            installStateListener = null
        }
    }
    
    /**
     * Hoàn thành update bằng cách restart app.
     * Phải gọi sau khi update đã được download (InstallStatus.DOWNLOADED).
     */
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
    
    /**
     * Kiểm tra xem update đã được download chưa (có thể do download từ lần trước).
     * Nên gọi trong onResume() của Activity.
     */
    fun checkIfUpdateDownloaded() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.i("InAppUpdate", "Update already downloaded")
                    _updateDownloaded.value = true
                    registerInstallStateListener() // Re-register để theo dõi
                }
            }
            .addOnFailureListener { e ->
                Log.e("InAppUpdate", "Failed to check if update downloaded", e)
            }
    }
    
    /**
     * Cleanup khi không cần dùng nữa.
     */
    fun cleanup() {
        unregisterInstallStateListener()
    }
    
    companion object {
        const val REQUEST_CODE_UPDATE = 1001
    }
}

