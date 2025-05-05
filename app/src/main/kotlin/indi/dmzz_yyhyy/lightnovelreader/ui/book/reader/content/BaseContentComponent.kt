package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun BaseContentComponent(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit,
    fontLineHeight: TextUnit,
    fontWeight: FontWeight,
    fontFamily: FontFamily?,
    color: Color
) {
    if (text.trim().startsWith("http://") || text.trim().startsWith("https://")) {
        Box(modifier
            .fillMaxWidth()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
            AsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(text)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )
        }
    } else
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