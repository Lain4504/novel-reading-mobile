package com.miraimagiclab.novelreadingapp.data.auth

import com.miraimagiclab.novelreadingapp.di.PublicClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApiService @Inject constructor(
    @PublicClient private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://ranoku.com" // Android emulator localhost
        private const val AUTH_PATH = "$BASE_URL/api/auth"
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> = runCatching {
        httpClient.post("$AUTH_PATH/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun register(
        email: String,
        username: String,
        password: String
    ): Result<RegisterResponse> = runCatching {
        httpClient.post("$AUTH_PATH/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, username, password))
        }.body()
    }

    suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponse> = runCatching {
        httpClient.post("$AUTH_PATH/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(TokenRefreshRequest(refreshToken))
        }.body()
    }

    suspend fun logout(): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/logout") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun getMe(): Result<UserInfo> = runCatching {
        httpClient.get("$AUTH_PATH/me").body()
    }

    suspend fun verifyEmail(email: String, otp: String): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(VerifyEmailRequest(email, otp))
        }.body()
    }

    suspend fun resendOtp(email: String, otpType: String = "EMAIL_VERIFICATION"): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/resend-otp") {
            contentType(ContentType.Application.Json)
            setBody(ResendOtpRequest(email, otpType))
        }.body()
    }

    suspend fun forgotPassword(email: String): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequest(email))
        }.body()
    }

    suspend fun resetPassword(
        email: String,
        otp: String,
        newPassword: String
    ): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(ResetPasswordRequest(email, otp, newPassword))
        }.body()
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<MessageResponse> = runCatching {
        httpClient.post("$AUTH_PATH/change-password") {
            contentType(ContentType.Application.Json)
            setBody(ChangePasswordRequest(currentPassword, newPassword))
        }.body()
    }
}
