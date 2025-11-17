package com.miraimagiclab.novelreadingapp.ui.book.reader

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.userdata.ColorUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPickerDialogViewModel @Inject constructor(
    val userDataRepository: UserDataRepository
) : ViewModel() {
    private var colorUserData: ColorUserData? = null

    fun init(colorUserDataPath: String): Flow<Color?>{
        colorUserData = userDataRepository.colorUserData(colorUserDataPath)
        return colorUserData!!.getFlow()
    }

    fun changeBackgroundColor(color: Color) {
        if (colorUserData == null) {
            Log.e("ColorPickerDialogViewModel", "change color user data before init!")
        }
        CoroutineScope(Dispatchers.IO).launch {
            colorUserData!!.set(color)
        }
    }
}