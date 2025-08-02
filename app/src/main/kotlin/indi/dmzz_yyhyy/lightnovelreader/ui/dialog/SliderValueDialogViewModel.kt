package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.FloatUserData
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class SliderValueDialogViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {
    private var floatUserData: FloatUserData? = null

    fun init(floatUserDataPath: String): Flow<Float> {
        floatUserData = userDataRepository.floatUserData(floatUserDataPath)
        println(floatUserDataPath)
        val value = floatUserData!!.getFlowWithDefault(Float.NaN).onEach { println("value got $it") }
        return value
    }

    fun setValue(value: Float) {
        if (floatUserData == null) {
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            println("SET VALUE $value")
            floatUserData?.asynchronousSet(value)
        }
    }
}
