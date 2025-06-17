package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.app.Activity
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ImageLayoutInfo(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onOriginalLayoutReady: (ImageLayoutInfo) -> Unit,
    onZoomEnd: (ImageLayoutInfo) -> Unit,
    minScaleForNavigate: Float = 1.1f,
    maxScale: Float = 5f,
    minScale: Float = 0.8f
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val localView = LocalView.current

    var layoutInfo by remember { mutableStateOf<ImageLayoutInfo?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val window = (localView.context as? Activity)?.window
                val location = IntArray(2)
                window?.decorView?.getLocationOnScreen(location)
                val size = coords.size
                val windowOffset = coords.localToWindow(Offset.Zero)
                val x = windowOffset.x + location[0]
                val y = windowOffset.y + location[1]
                val info = ImageLayoutInfo(x = x, y = y, width = size.width.toFloat(), height = size.height.toFloat())
                layoutInfo = info
                onOriginalLayoutReady(info)
            }
            .pointerInput(imageUrl) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                        if (down.changes.isEmpty()) continue

                        var pastTouchSlop = false
                        var cumulativeScale = 1f
                        var cumulativeOffset = Offset.Zero
                        val initialScale = scale
                        val initialOffset = offset

                        do {
                            val event = awaitPointerEvent()
                            if (event.changes.size >= 2) {
                                event.changes.forEach { it.consume() }
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                cumulativeScale *= zoomChange
                                cumulativeOffset += panChange

                                val newScale = (initialScale * cumulativeScale).coerceIn(minScale, maxScale)
                                scale = newScale
                                offset = initialOffset + cumulativeOffset
                                pastTouchSlop = true
                            }
                        } while (event.changes.any { it.pressed })

                        if (pastTouchSlop && cumulativeScale > minScaleForNavigate) {
                            layoutInfo?.let { orig ->
                                val displayedWidth = orig.width * scale
                                val displayedHeight = orig.height * scale
                                val displayedX = orig.x - (displayedWidth - orig.width) / 2f + offset.x
                                val displayedY = orig.y - (displayedHeight - orig.height) / 2f + offset.y

                                val zoomLayout = ImageLayoutInfo(
                                    x = displayedX,
                                    y = displayedY,
                                    width = displayedWidth,
                                    height = displayedHeight
                                )
                                onZoomEnd(zoomLayout)
                            }
                        }
                        coroutineScope.launch {
                            delay(200)
                            scale = 1f
                            offset = Offset.Zero
                        }
                    }
                }
            }
            .zIndex(if (scale != 1f) 2f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = null
        )
    }
}