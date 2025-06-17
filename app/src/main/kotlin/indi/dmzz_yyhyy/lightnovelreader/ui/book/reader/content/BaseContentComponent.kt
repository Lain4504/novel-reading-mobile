package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToImageViewerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ImageLayoutInfo
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ZoomableImage

@Composable
fun BaseContentComponent(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit,
    fontLineHeight: TextUnit,
    fontWeight: FontWeight,
    fontFamily: FontFamily?,
    color: Color
) {
    val trimmed = text.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        val navController = LocalNavController.current
        var originalLayout by remember { mutableStateOf<ImageLayoutInfo?>(null) }

        ZoomableImage(
            imageUrl = trimmed,
            modifier = imageModifier,
            onOriginalLayoutReady = { layoutInfo ->
                originalLayout = layoutInfo
            },
            onZoomEnd = { zoomLayout ->
                originalLayout?.let { orig ->
                    navController.navigateToImageViewerDialog( // FIXME: use callback!! FIXME FIXME
                        imageUrl = trimmed,
                        originalLayout = orig,
                        startLayout = zoomLayout
                    )
                }
            }
        )
    } else {
        SelectionContainer {
            Text(
                modifier = modifier.fillMaxSize(),
                text = text,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = fontWeight,
                fontSize = fontSize,
                fontFamily = fontFamily,
                color = color,
                lineHeight = (fontSize.value + fontLineHeight.value).sp
            )
        }
    }
}