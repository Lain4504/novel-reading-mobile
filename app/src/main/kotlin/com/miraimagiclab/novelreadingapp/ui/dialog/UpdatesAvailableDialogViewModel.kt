package com.miraimagiclab.novelreadingapp.ui.dialog

import androidx.lifecycle.ViewModel
import com.miraimagiclab.novelreadingapp.data.update.UpdateCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdatesAvailableDialogViewModel @Inject constructor(
    private val updateCheckRepository: UpdateCheckRepository
) : ViewModel() {
    val release = updateCheckRepository.release
    val availableFlow = updateCheckRepository.availableFlow
    val updatePhaseFlow = updateCheckRepository.updatePhase

    fun downloadUpdate() =
        updateCheckRepository.downloadUpdate()

    fun resetAvailable() = updateCheckRepository.resetAvailable()
}