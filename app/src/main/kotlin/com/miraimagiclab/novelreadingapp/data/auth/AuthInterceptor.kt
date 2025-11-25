package com.miraimagiclab.novelreadingapp.data.auth

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthInterceptor(
    private val tokenManager: TokenManager
) {
    private val refreshMutex = Mutex()

    val plugin: ClientPlugin<Unit> = createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            // Skip adding auth header for auth endpoints
            if (shouldSkipAuthForBuilder(request)) {
                return@onRequest
            }

            // Get valid access token (will auto-refresh if needed)
            val accessToken = tokenManager.getValidAccessToken()
            if (!accessToken.isNullOrEmpty()) {
                request.header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        onResponse { response ->
            // Handle 401 Unauthorized - token might be invalid
            if (response.status == HttpStatusCode.Unauthorized) {
                // Check if this is not an auth endpoint to avoid infinite loops
                val request = response.call.request
                if (!shouldSkipAuthForRequest(request)) {
                    // Try to refresh the token
                    refreshMutex.withLock {
                        val newToken = tokenManager.refreshAccessToken()
                        if (newToken.isNullOrEmpty()) {
                            // Refresh failed, user needs to re-authenticate
                            tokenManager.clearAuth()
                        }
                    }
                }
            }
        }
    }

    private fun shouldSkipAuthForBuilder(request: HttpRequestBuilder): Boolean {
        val url = request.url.buildString()
        return isAuthEndpoint(url)
    }

    private fun shouldSkipAuthForRequest(request: HttpRequest): Boolean {
        val url = request.url.toString()
        return isAuthEndpoint(url)
    }

    private fun isAuthEndpoint(url: String): Boolean {
        return url.contains("/api/auth/login") ||
                url.contains("/api/auth/register") ||
                url.contains("/api/auth/refresh-token") ||
                url.contains("/api/auth/verify-email") ||
                url.contains("/api/auth/resend-otp") ||
                url.contains("/api/auth/forgot-password") ||
                url.contains("/api/auth/reset-password")
    }
}
