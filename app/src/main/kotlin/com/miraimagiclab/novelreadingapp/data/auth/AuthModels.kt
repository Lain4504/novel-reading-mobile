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
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType") val tokenType: String? = "Bearer",
    @SerialName("userId") val userId: String,
    @SerialName("username") val username: String,
    @SerialName("email") val email: String
) {
    // Backward compatibility: use userId as id
    val id: String get() = userId
}

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val email: String
)

@Serializable
data class TokenRefreshResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType") val tokenType: String? = "Bearer"
)

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class UserInfo(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("email") val email: String,
    @SerialName("roles") val roles: List<String>? = null,
    @SerialName("banned") val banned: Boolean? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("backgroundUrl") val backgroundUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("socialLinks") val socialLinks: List<SocialLinkDto>? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("lastTimeLogin") val lastTimeLogin: String? = null,
    @SerialName("lockUntil") val lockUntil: String? = null,
    @SerialName("lockReason") val lockReason: String? = null
)

@Serializable
data class SocialLinkDto(
    @SerialName("platform") val platform: String,
    @SerialName("url") val url: String
)

// Error Response
@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val success: Boolean = false
)
