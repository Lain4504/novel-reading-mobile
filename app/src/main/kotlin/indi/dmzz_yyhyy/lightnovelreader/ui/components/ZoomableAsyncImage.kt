package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onZoomEnd: () -> Unit,
    maxScale: Float = 5f,
    minScale: Float = 1f,
    header: Map<String, String>
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .pointerInput(imageUrl) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                        if (down.changes.isEmpty()) continue

                        var pastTouchSlop = false
                        var cumulativeScale = 1f
                        val initialScale = scale

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.size >= 2) {
                                event.changes.forEach { it.consume() }
                                val zoomChange = event.calculateZoom()
                                cumulativeScale *= zoomChange
                                scale = (initialScale * cumulativeScale).coerceIn(minScale, maxScale)
                                pastTouchSlop = true
                            }
                        } while (event.changes.any { it.pressed })

                        if (pastTouchSlop) {
                            onZoomEnd()
                            coroutineScope.launch {
                                delay(200)
                                scale = 1f
                            }
                        }
                    }
                }
            }
            .zIndex(if (scale > 1f) 2f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 64.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .align(Alignment.Center),
            model = ImageRequest.Builder(LocalContext.current)
                .also { builder ->
                    header.forEach {
                        builder.addHeader(it.key, it.value)
                    }
                }
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success
                    || state is AsyncImagePainter.State.Error
                ) {
                    isLoading = false
                }
            }
        )
    }
}

