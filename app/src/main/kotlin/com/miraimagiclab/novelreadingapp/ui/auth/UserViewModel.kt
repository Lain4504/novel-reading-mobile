package com.miraimagiclab.novelreadingapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraimagiclab.novelreadingapp.data.auth.UserInfo
import com.miraimagiclab.novelreadingapp.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val user: UserInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        // Observe authentication state
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuthenticated ->
                _uiState.value = _uiState.value.copy(isAuthenticated = isAuthenticated)
                if (isAuthenticated) {
                    fetchCurrentUser()
                } else {
                    _uiState.value = _uiState.value.copy(user = null)
                }
            }
        }
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            if (!authRepository.isUserAuthenticated()) {
                _uiState.value = _uiState.value.copy(
                    user = null,
                    isLoading = false,
                    isAuthenticated = false
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.getCurrentUser().first()
            result.fold(
                onSuccess = { userInfo ->
                    _uiState.value = _uiState.value.copy(
                        user = userInfo,
                        isLoading = false,
                        error = null,
                        isAuthenticated = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải thông tin người dùng",
                        isAuthenticated = false
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

