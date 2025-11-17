package com.miraimagiclab.novelreadingapp.data.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object LocalDateTimeTypeAdapter: TypeAdapter<LocalDateTime>() {
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        out.value(value?.toString()) // -> "2025-05-08T13:16:33" (ISO)
    }

    override fun read(reader: JsonReader): LocalDateTime {
        return LocalDateTime.parse(reader.nextString())
    }
}

object LocalDateTypeAdapter: TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        out.value(value?.toString()) // -> "2025-05-08"
    }

    override fun read(reader: JsonReader): LocalDate {
        return LocalDate.parse(reader.nextString())
    }
}

object LocalTimeTypeAdapter: TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter, value: LocalTime?) {
        out.value(value?.toString()) // -> "13:16:33"
    }

    override fun read(reader: JsonReader): LocalTime {
        return LocalTime.parse(reader.nextString())
    }
}
