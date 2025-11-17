package io.lain4504.novelreadingapp.api.content.component

import kotlinx.serialization.json.JsonElement

interface ComponentDataJsonElementSerializer<Data> {
    fun toJsonElement(data: Data): JsonElement
    fun fromJsonElement(json: JsonElement): Data
}