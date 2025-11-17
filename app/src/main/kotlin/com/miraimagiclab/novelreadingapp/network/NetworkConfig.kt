package com.miraimagiclab.novelreadingapp.network

object NetworkConfig {
    const val BASE_URL = "https://ranoku.com"
    const val GRAPHQL_ENDPOINT = "$BASE_URL/graphql"
    const val REST_API_BASE = "$BASE_URL/api"
    
    // Timeout settings
    const val CONNECT_TIMEOUT_MS = 30_000L
    const val SOCKET_TIMEOUT_MS = 30_000L
}

