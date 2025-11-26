package com.miraimagiclab.novelreadingapp.domain.auth

import com.miraimagiclab.novelreadingapp.data.auth.AuthApiService
import com.miraimagiclab.novelreadingapp.data.auth.LoginResponse
import com.miraimagiclab.novelreadingapp.data.auth.MessageResponse
import com.miraimagiclab.novelreadingapp.data.auth.RegisterResponse
import com.miraimagiclab.novelreadingapp.data.auth.TokenManager
import com.miraimagiclab.novelreadingapp.data.auth.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    val isAuthenticated: StateFlow<Boolean> = tokenManager.isAuthenticated

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return authApiService.login(email, password).onSuccess { response ->
            tokenManager.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                userId = response.userId,
                username = response.username,
                email = response.email
            )
        }
    }

    suspend fun register(email: String, username: String, password: String): Result<RegisterResponse> {
        return authApiService.register(email, username, password)
    }

    suspend fun verifyEmail(email: String, otp: String): Result<MessageResponse> {
        return authApiService.verifyEmail(email, otp)
    }

    suspend fun resendOtp(email: String, otpType: String = "EMAIL_VERIFICATION"): Result<MessageResponse> {
        return authApiService.resendOtp(email, otpType)
    }

    suspend fun forgotPassword(email: String): Result<MessageResponse> {
        return authApiService.forgotPassword(email)
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<MessageResponse> {
        return authApiService.resetPassword(email, otp, newPassword)
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<MessageResponse> {
        return authApiService.changePassword(currentPassword, newPassword)
    }

    suspend fun logout(): Result<MessageResponse> {
        val result = authApiService.logout()
        tokenManager.clearAuth()
        return result
    }

    suspend fun getMe(): Result<UserInfo> {
        return authApiService.getMe()
    }

    fun getCurrentUser(): Flow<Result<UserInfo>> = flow {
        if (!tokenManager.isUserAuthenticated()) {
            emit(Result.failure(Exception("User not authenticated")))
            return@flow
        }
        emit(getMe())
    }

    fun getUserId(): String? = tokenManager.getUserId()
    fun getUsername(): String? = tokenManager.getUsername()
    fun getEmail(): String? = tokenManager.getEmail()
    fun isUserAuthenticated(): Boolean = tokenManager.isUserAuthenticated()
}
