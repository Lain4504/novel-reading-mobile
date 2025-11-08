package indi.dmzz_yyhyy.lightnovelreader.serialization

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

inline fun <reified T : Any> serializableType(
    serializable: KSerializer<T>,
    isNullableAllowed: Boolean = false,
    json: Json = Json,
) = object : NavType<T>(isNullableAllowed = isNullableAllowed) {

    override fun put(bundle: SavedState, key: String, value: T) {
        bundle.write { putString(key, json.encodeToString(serializable, value)) }
    }

    override fun get(bundle: SavedState, key: String): T? {
        return json.decodeFromString(serializable, bundle.read { getString(key) })
    }

    override fun parseValue(value: String): T = json.decodeFromString(serializable, value)

    override fun serializeAsValue(value: T): String = json.encodeToString(serializable, value)
}
