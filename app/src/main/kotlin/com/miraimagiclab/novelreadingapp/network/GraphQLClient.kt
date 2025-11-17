package com.miraimagiclab.novelreadingapp.network

import com.miraimagiclab.novelreadingapp.network.models.GraphQLRequest
import com.miraimagiclab.novelreadingapp.network.models.GraphQLResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphQLClient @Inject constructor() {
    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    /**
     * Execute GraphQL query
     */
    suspend inline fun <reified T> query(
        query: String,
        variables: Map<String, JsonElement>? = null,
        operationName: String? = null
    ): Result<T> {
        return try {
            val request = GraphQLRequest(
                query = query,
                variables = variables,
                operationName = operationName
            )
            
            val response: GraphQLResponse<T> = httpClient.post {
                url(NetworkConfig.GRAPHQL_ENDPOINT)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            if (response.errors != null && response.errors.isNotEmpty()) {
                val errorMessage = response.errors.joinToString { it.message }
                Result.failure(Exception("GraphQL Error: $errorMessage"))
            } else if (response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("No data returned from GraphQL query"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
}

