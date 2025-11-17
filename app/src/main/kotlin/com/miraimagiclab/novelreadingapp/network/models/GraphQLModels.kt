package com.miraimagiclab.novelreadingapp.network.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * GraphQL Request
 */
@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, JsonElement>? = null,
    val operationName: String? = null
)

/**
 * GraphQL Response
 */
@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<ErrorLocation>? = null,
    val path: List<String>? = null
)

@Serializable
data class ErrorLocation(
    val line: Int,
    val column: Int
)

