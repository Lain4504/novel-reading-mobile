package com.miraimagiclab.novelreadingapp.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Request Models
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class TokenRefreshRequest(
    val refreshToken: String
)

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val otp: String
)

@Serializable
data class ResendOtpRequest(
    val email: String,
    val otpType: String = "EMAIL_VERIFICATION"
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// Response Models
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val id: String,
    val username: String,
    val email: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val email: String
)

@Serializable
data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val verified: Boolean? = null,
    val createdAt: String? = null
)

// Error Response
@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val success: Boolean = false
)
