package com.miraimagiclab.novelreadingapp.ui.dialog

import androidx.lifecycle.ViewModel
import com.miraimagiclab.novelreadingapp.data.update.InAppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdatesAvailableDialogViewModel @Inject constructor(
    private val inAppUpdateRepository: InAppUpdateRepository
) : ViewModel() {
    val updateAvailable = inAppUpdateRepository.updateAvailable
    val updateDownloaded = inAppUpdateRepository.updateDownloaded
    val installState = inAppUpdateRepository.installState
    
    fun completeUpdate() = inAppUpdateRepository.completeUpdate()
}
