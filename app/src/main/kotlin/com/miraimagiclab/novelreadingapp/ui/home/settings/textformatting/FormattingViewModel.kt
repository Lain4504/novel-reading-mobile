package com.miraimagiclab.novelreadingapp.ui.home.settings.textformatting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.book.BookRepository
import com.miraimagiclab.novelreadingapp.data.format.FormatRepository
import com.miraimagiclab.novelreadingapp.data.format.FormattingGroup
import com.miraimagiclab.novelreadingapp.data.format.FormattingRule
import dagger.hilt.android.lifecycle.HiltViewModel
import io.lain4504.novelreadingapp.api.book.BookInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormattingViewModel @Inject constructor(
    private val formattingRepository: FormatRepository,
    bookRepository: BookRepository
) : ViewModel() {
    var formattingGroups by mutableStateOf(emptyList<FormattingGroup>())
        private set
    var bookId = ""
    var rules by mutableStateOf(listOf<FormattingRule>())
        private set
    val bookInformationMap = mutableStateMapOf<String, BookInformation>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { formattingRepository.getFormattingMap() }.collect { map ->
                formattingGroups = map.map {
                    FormattingGroup(it.key, it.value.size)
                }
                for (group in formattingGroups) {
                    if (group.id.isBlank()) continue
                    bookInformationMap[group.id] = bookRepository.getStateBookInformation(group.id, viewModelScope)
                }
            }
        }
    }

    fun loadBookFormattingRules(bookId: String) {
        this.bookId = bookId
        rules = formattingRepository.getStateBookFormattingRules(bookId)
    }
    fun onToggle(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            formattingRepository.updateRule(
                bookId = bookId,
                formattingRule = rules
                    .firstOrNull { it.id == id }
                    ?.let {
                        it.copy(
                            isEnabled = !it.isEnabled
                        )
                    } ?: return@launch
            )
        }
    }
}
