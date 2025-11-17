package com.miraimagiclab.novelreadingapp.data.content

import android.content.Context
import com.miraimagiclab.novelreadingapp.data.content.component.ErrorContentComponent
import com.miraimagiclab.novelreadingapp.data.content.component.ImageComponent
import com.miraimagiclab.novelreadingapp.data.content.component.SimpleTextComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.lain4504.novelreadingapp.api.content.ContentComponentRepositoryApi
import io.lain4504.novelreadingapp.api.content.ContentData
import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponent
import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponentData
import io.lain4504.novelreadingapp.api.content.component.ComponentDataJsonElementSerializer
import io.lain4504.novelreadingapp.api.content.component.ImageComponentData
import io.lain4504.novelreadingapp.api.content.component.SimpleTextComponentData
import io.lain4504.novelreadingapp.api.userdata.UserDataRepositoryApi
import io.lain4504.novelreadingapp.api.web.WebBookDataSourceManagerApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class ContentComponentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDataRepositoryApi: UserDataRepositoryApi,
    private val webBookDataSourceManagerApi: WebBookDataSourceManagerApi
): ContentComponentRepositoryApi {
    private val serializeMutableMap = mutableMapOf<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>()
    val serializeMap = serializeMutableMap.toMap()
    private val kClassMutableMap = mutableMapOf<String, KClass<out AbstractContentComponent<out AbstractContentComponentData>>>()
    private val dataKClassMutableMap = mutableMapOf<String, KClass<out AbstractContentComponentData>>()
    val dataKClassMap = dataKClassMutableMap.toMap()

    fun getContentDataFromJson(jsonObject: JsonObject): ContentData = ContentData(
        jsonObject["components"]
            ?.jsonArray
            ?.mapNotNull { it.jsonObject }
            ?.map {
                val id = it["id"]?.jsonPrimitive?.content
                    ?: return@map ErrorContentComponent.Companion.of("component id not found")
                val data = it["data"]?.jsonObject
                    ?: return@map ErrorContentComponent.Companion.of("component data not found\nid=$id")
                val kClass = kClassMutableMap[id]
                    ?: return@map ErrorContentComponent.Companion.of("component class not found\nid=$id")
                val dataKClass = dataKClassMutableMap[id]
                    ?: return@map ErrorContentComponent.Companion.of("component data class not found\nid=$id")
                val serializer = serializeMutableMap[id]
                    ?: return@map ErrorContentComponent.Companion.of("component data serializer not found\nid=$id")
                
                // Instantiate component directly based on registered type
                val componentData = serializer.fromJsonElement(data)
                val component = when (kClass) {
                    SimpleTextComponent::class -> {
                        SimpleTextComponent(
                            componentData as SimpleTextComponentData,
                            userDataRepositoryApi,
                            context
                        )
                    }
                    ImageComponent::class -> {
                        ImageComponent(
                            componentData as ImageComponentData,
                            webBookDataSourceManagerApi
                        )
                    }
                    else -> return@map ErrorContentComponent.Companion.of("unsupported component type: ${kClass.simpleName}")
                }
                return@map component as AbstractContentComponent<out AbstractContentComponentData>
            } ?: listOf(ErrorContentComponent.Companion.of("error to load components from json"))
    )

    interface Registrar: ContentComponentRepositoryApi.Registrar {
        override fun id(id: String): RegisterBuilder
    }

    override val registrar = object: Registrar {
        override fun id(id: String) = RegisterBuilder(serializeMutableMap, kClassMutableMap, dataKClassMutableMap, id)
    }

    @Suppress("UNCHECKED_CAST")
    class RegisterBuilder(
        private val serializerMap: MutableMap<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>,
        private val kClassMap: MutableMap<String, KClass<out AbstractContentComponent<out AbstractContentComponentData>>>,
        private val dataKClassMap: MutableMap<String, KClass<out AbstractContentComponentData>>,
        val id: String
    ): ContentComponentRepositoryApi.RegisterBuilder {
        var componentKClass: KClass<out AbstractContentComponent<out AbstractContentComponentData>>? = null
        var componentDataKClass: KClass<out AbstractContentComponentData>? = null
        var serializer: ComponentDataJsonElementSerializer<out AbstractContentComponentData>? = null

        override fun component(value: KClass<out AbstractContentComponent<out AbstractContentComponentData>>): RegisterBuilder {
            this.componentKClass = value
            return this
        }

        override fun data(value: KClass<out AbstractContentComponentData>): RegisterBuilder {
            this.componentDataKClass = value
            return this
        }

        override fun serializer(value: ComponentDataJsonElementSerializer<out AbstractContentComponentData>): RegisterBuilder {
            this.serializer = value
            return this
        }

        override fun register() {
            if (componentKClass == null || componentDataKClass == null ||serializer == null) throw Error("builder missing parameters")
            kClassMap.put(id, componentKClass!!)
            dataKClassMap.put(id, componentDataKClass!!)
            serializerMap.put(id, serializer!!)
        }
    }
    fun getDataFromJsonObject(content: JsonObject, block: (AbstractContentComponentData) -> Unit) {
        content["components"]
            ?.jsonArray
            ?.mapNotNull { it.jsonObject }
            ?.forEach {
                val id = it["id"]?.jsonPrimitive?.content
                    ?: return@forEach
                val data = it["data"]?.jsonObject
                    ?: return@forEach
                val serializer = serializeMap[id]
                    ?: return@forEach
                block(serializer.fromJsonElement(data))
            }
    }

    init {
        registrar
            .id(SimpleTextComponentData.ID)
            .component(SimpleTextComponent::class)
            .data(SimpleTextComponentData::class)
            .serializer(SimpleTextComponentData.jsonSerializer)
            .register()

        registrar
            .id(ImageComponentData.ID)
            .component(ImageComponent::class)
            .data(ImageComponentData::class)
            .serializer(ImageComponentData.jsonSerializer)
            .register()
    }

}