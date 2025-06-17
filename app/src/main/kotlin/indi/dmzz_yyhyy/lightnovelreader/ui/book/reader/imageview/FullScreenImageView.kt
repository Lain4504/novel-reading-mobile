package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun ImageViewerScreen(
    imageUrl: String,
    originalX: Float,
    originalY: Float,
    originalWidth: Float,
    originalHeight: Float,
    startX: Float,
    startY: Float,
    startWidth: Float,
    startHeight: Float,
    onDismissRequest: () -> Unit
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }

    val originalCenter = remember {
        Offset(originalX + originalWidth / 2f, originalY + originalHeight / 2f)
    }
    val startCenter = remember {
        Offset(startX + startWidth / 2f, startY + startHeight / 2f)
    }
    val initScale = remember(startWidth, screenWidth) { (startWidth / screenWidth).coerceAtLeast(0.01f) }
    val initOffset = remember(startCenter, screenWidth, screenHeight) {
        Offset(startCenter.x - screenWidth / 2f, startCenter.y - screenHeight / 2f)
    }

    val scaleAnim = remember { Animatable(initScale) }
    val offsetAnim = remember { Animatable(initOffset, Offset.VectorConverter) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    fun constrain() {
        if (imageSize == Size.Zero) return
        val sw = imageSize.width * scaleAnim.value
        val sh = imageSize.height * scaleAnim.value
        val maxX = maxOf(0f, (sw - screenWidth) / 2f)
        val maxY = maxOf(0f, (sh - screenHeight) / 2f)
        coroutineScope.launch {
            val ox = offsetAnim.value.x.coerceIn(-maxX, maxX)
            val oy = offsetAnim.value.y.coerceIn(-maxY, maxY)
            offsetAnim.snapTo(Offset(ox, oy))
        }
    }

    LaunchedEffect(scaleAnim.value) {
        if (scaleAnim.value < 0.8f) {
            onDismissRequest()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        coroutineScope.launch {
                            if (imageSize != Size.Zero) {
                                val targetScale = (originalWidth / imageSize.width).coerceAtLeast(0.01f)
                                val targetOffset = Offset(originalCenter.x - screenWidth / 2f, originalCenter.y - screenHeight / 2f)
                                val sJob = async { scaleAnim.animateTo(targetScale, animationSpec = tween(300)) }
                                val oJob = async { offsetAnim.animateTo(targetOffset, animationSpec = tween(300)) }
                                sJob.await(); oJob.await()
                            }
                            onDismissRequest()
                        }
                    },
                    onDoubleTap = { tap ->
                        coroutineScope.launch {
                            if (imageSize == Size.Zero) return@launch
                            val fitScale = screenWidth / imageSize.width
                            val curScale = scaleAnim.value
                            if (curScale > fitScale * 1.1f) {
                                val sJob = async { scaleAnim.animateTo(fitScale, animationSpec = tween(300)) }
                                val oJob = async { offsetAnim.animateTo(Offset.Zero, animationSpec = tween(300)) }
                                sJob.await(); oJob.await()
                            } else {
                                val targetScale = fitScale * 2f
                                val dx = tap.x - (screenWidth / 2f + offsetAnim.value.x)
                                val dy = tap.y - (screenHeight / 2f + offsetAnim.value.y)
                                val ratio = targetScale / curScale
                                val newOff = Offset(
                                    offsetAnim.value.x - dx * (ratio - 1f),
                                    offsetAnim.value.y - dy * (ratio - 1f)
                                )
                                val sJob = async { scaleAnim.animateTo(targetScale, animationSpec = tween(300)) }
                                val oJob = async { offsetAnim.animateTo(newOff, animationSpec = tween(300)) }
                                sJob.await(); oJob.await()
                                constrain()
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    coroutineScope.launch {
                        val ns = (scaleAnim.value * zoom).coerceIn(0.5f, 5f)
                        scaleAnim.snapTo(ns)
                        offsetAnim.snapTo(offsetAnim.value + pan)
                        constrain()
                    }
                }
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .onGloballyPositioned { coords ->
                    imageSize = Size(coords.size.width.toFloat(), coords.size.height.toFloat())
                }
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = offsetAnim.value.x
                    translationY = offsetAnim.value.y
                }
        )

        IconButton(
            onClick = onDismissRequest,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .systemBarsPadding()
                .background(Color.White, CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.close_24px),
                contentDescription = "close",
                tint = Color.Black
            )
        }
    }
}
