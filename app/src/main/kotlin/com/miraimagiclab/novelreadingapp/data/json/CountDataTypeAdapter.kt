package com.miraimagiclab.novelreadingapp.data.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.miraimagiclab.novelreadingapp.data.statistics.Count

object CountBase64TypeAdapter : TypeAdapter<Count>() {
    override fun write(out: JsonWriter?, value: Count?) {
        out?.value(value?.toBase64String())
    }

    override fun read(`in`: JsonReader?): Count {
        return Count.Companion.fromBase64String(`in`?.nextString() ?: "")
    }
}
