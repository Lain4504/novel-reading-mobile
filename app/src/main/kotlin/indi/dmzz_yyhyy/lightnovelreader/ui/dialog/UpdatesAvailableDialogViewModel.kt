package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.update.Release
import indi.dmzz_yyhyy.lightnovelreader.data.update.UpdateCheckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdatesAvailableDialogViewModel @Inject constructor(
    private val updateCheckRepository: UpdateCheckRepository
) : ViewModel() {
    var release by mutableStateOf<Release?>(null)
        private set

    init {
        release = updateCheckRepository.release
        viewModelScope.launch {
            updateCheckRepository.releaseFlow.collect {
                release = it
            }
        }
    }

    fun downloadUpdate(release: Release?, context: Context) =
        updateCheckRepository.downloadUpdate(release?.downloadSize ?: "", release?.version.toString(), release?.checksum ?: "", context)

    fun checkUpdate() = viewModelScope.launch(Dispatchers.IO) { updateCheckRepository.checkUpdate() }
}