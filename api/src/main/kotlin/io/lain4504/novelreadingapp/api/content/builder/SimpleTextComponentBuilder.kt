package io.lain4504.novelreadingapp.api.content.builder

import io.lain4504.novelreadingapp.api.content.component.SimpleTextComponentData

fun ContentBuilder.simpleText(text: String): ContentBuilder = component(SimpleTextComponentData(text))