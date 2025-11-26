package com.miraimagiclab.novelreadingapp.data.auth

import com.miraimagiclab.novelreadingapp.di.PublicClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val tokenStorage: TokenStorage,
    @PublicClient private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://ranoku.com"
        private const val AUTH_PATH = "$BASE_URL/api/auth"
    }
    private val refreshMutex = Mutex()
    private var isRefreshing = false

    // Access token expiration time in milliseconds (default: 1 hour)
    private val accessTokenExpirationMs = 60 * 60 * 1000L

    val isAuthenticated: StateFlow<Boolean> = tokenStorage.isAuthenticated

    suspend fun getValidAccessToken(): String? {
        val accessToken = tokenStorage.getAccessToken()
        
        if (accessToken.isNullOrEmpty()) {
            return null
        }

        // Check if token is expired or about to expire (5 minutes buffer)
        val tokenSavedAt = tokenStorage.getAccessTokenSavedTime()
        val now = System.currentTimeMillis()
        val tokenAge = now - tokenSavedAt
        val bufferTime = 5 * 60 * 1000L // 5 minutes

        if (tokenAge >= (accessTokenExpirationMs - bufferTime)) {
            // Token is expired or about to expire, refresh it
            return refreshAccessToken()
        }

        return accessToken
    }

    suspend fun refreshAccessToken(): String? = refreshMutex.withLock {
        // Double-check if another coroutine already refreshed the token
        val currentToken = tokenStorage.getAccessToken()
        val tokenSavedAt = tokenStorage.getAccessTokenSavedTime()
        val now = System.currentTimeMillis()
        val tokenAge = now - tokenSavedAt
        val bufferTime = 5 * 60 * 1000L

        if (!currentToken.isNullOrEmpty() && tokenAge < (accessTokenExpirationMs - bufferTime)) {
            // Token was already refreshed by another coroutine
            return currentToken
        }

        val refreshToken = tokenStorage.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            clearAuth()
            return null
        }

        isRefreshing = true
        return try {
            val result = runCatching {
                httpClient.post("$AUTH_PATH/refresh-token") {
                    contentType(ContentType.Application.Json)
                    setBody(TokenRefreshRequest(refreshToken))
                }.body<TokenRefreshResponse>()
            }
            
            result.fold(
                onSuccess = { response ->
                    tokenStorage.saveAccessToken(response.accessToken)
                    response.accessToken
                },
                onFailure = { error ->
                    // Refresh token is invalid or expired, clear auth
                    clearAuth()
                    null
                }
            )
        } finally {
            isRefreshing = false
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String, userId: String, username: String, email: String) {
        tokenStorage.saveTokens(accessToken, refreshToken)
        tokenStorage.saveUserInfo(userId, username, email)
    }

    fun clearAuth() {
        tokenStorage.clearTokens()
    }

    fun isUserAuthenticated(): Boolean {
        return tokenStorage.hasValidTokens()
    }

    fun getUserId(): String? = tokenStorage.getUserId()
    fun getUsername(): String? = tokenStorage.getUsername()
    fun getEmail(): String? = tokenStorage.getEmail()
}
