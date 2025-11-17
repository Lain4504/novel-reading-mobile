package io.lain4504.novelreadingapp.api.content.builder

import android.net.Uri
import io.lain4504.novelreadingapp.api.content.component.ImageComponentData

fun ContentBuilder.image(uri: Uri): ContentBuilder = component(ImageComponentData(uri))