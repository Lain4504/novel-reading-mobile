package io.lain4504.novelreadingapp.api.content

import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponent
import io.lain4504.novelreadingapp.api.content.component.AbstractContentComponentData
import io.lain4504.novelreadingapp.api.content.component.ComponentDataJsonElementSerializer
import kotlin.reflect.KClass

interface ContentComponentRepositoryApi {
    interface RegisterBuilder {
        fun component(value: KClass<out AbstractContentComponent<out AbstractContentComponentData>>): RegisterBuilder

        fun data(value: KClass<out AbstractContentComponentData>): RegisterBuilder

        fun serializer(value: ComponentDataJsonElementSerializer<out AbstractContentComponentData>): RegisterBuilder

        fun register()
    }

    interface Registrar {
        fun id(id: String): RegisterBuilder
    }

    val registrar: Registrar
}