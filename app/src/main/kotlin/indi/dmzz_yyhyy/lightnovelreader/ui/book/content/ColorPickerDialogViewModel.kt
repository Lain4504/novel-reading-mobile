package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPickerDialogViewModel @Inject constructor(
    userDataRepository: UserDataRepository
) : ViewModel() {
    private val backgroundColorUserData = userDataRepository.colorUserData(UserDataPath.Reader.BackgroundColor.path)
    val selectedColorFlow = backgroundColorUserData.getFlow()

    fun changeBackgroundColor(color: Color) {
        CoroutineScope(Dispatchers.IO).launch {
            backgroundColorUserData.set(color)
        }
    }
}