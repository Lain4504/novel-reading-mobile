package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count

object CountBase64TypeAdapter : TypeAdapter<Count>() {
    override fun write(out: JsonWriter?, value: Count?) {
        out?.value(value?.toBase64String())
    }

    override fun read(`in`: JsonReader?): Count {
        return Count.fromBase64String(`in`?.nextString() ?: "")
    }
}
