package com.miraimagiclab.novelreadingapp.ui.home.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.userdata.UserDataRepository
import com.miraimagiclab.novelreadingapp.ui.book.reader.SettingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val settingState = SettingState(
        userDataRepository,
        viewModelScope
    )
}