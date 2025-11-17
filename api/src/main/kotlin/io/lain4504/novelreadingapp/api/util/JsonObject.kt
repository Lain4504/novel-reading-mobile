package io.lain4504.novelreadingapp.api.util

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

fun JsonObject.Companion.empty() = buildJsonObject {  }