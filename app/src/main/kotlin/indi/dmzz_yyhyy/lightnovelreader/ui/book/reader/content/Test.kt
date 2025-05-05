package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun Test() {
    val items = remember { mutableStateListOf(0L) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(
                items = items,
                key = { it }
            ) {
                Text(it.toString())
            }
        }
        Row {
            Button(
                {
                    items.removeAt(0)
                }
            ) {
                Text("remove last")
            }
            Button(
                {
                    items.add(System.currentTimeMillis())
                }
            ) {
                Text("add next")
            }
        }
    }
}