package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()
private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
    context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

@Composable
private fun Last7DaysChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    data: Map<String, Int>
) {
    CartesianChartHost(
        rememberCartesianChart(
            rememberColumnCartesianLayer(
                ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = fill(Color(0xff916cda)), thickness = 16.dp)
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis =
            HorizontalAxis.rememberBottom(
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
                valueFormatter = BottomAxisValueFormatter,
            ),
            layerPadding = { cartesianLayerPadding(scalableStart = 8.dp, scalableEnd = 8.dp) },
            decorations = listOf(horizontalLine(data)),
        ),
        modelProducer = modelProducer,
        modifier = modifier.height(224.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
    )
}

@Composable
private fun horizontalLine(data: Map<String, Int>): HorizontalLine {
    val fill = fill(Color(0xfffdc8c4))
    val line = rememberLineComponent(fill = fill, thickness = 2.dp)
    val labelComponent =
        rememberTextComponent(
            margins = insets(start = 6.dp),
            padding = insets(start = 8.dp, end = 8.dp, bottom = 2.dp),
            background =
            shapeComponent(fill, CorneredShape.rounded(bottomLeft = 4.dp, bottomRight = 4.dp)),
        )
    val values = data.values
    val average = if (values.isNotEmpty()) values.average() else 0.0
    println("AVG is $average")

    return HorizontalLine(
        y = { average },
        line = line,
        labelComponent = labelComponent,
        label = { "%.1f 分钟".format(average) },
        verticalLabelPosition = Position.Vertical.Bottom,
    )
}

@Composable
fun Last7DaysChart(modifier: Modifier = Modifier, data: Map<String, Int>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.values) }
            extras { it[BottomAxisLabelKey] = data.keys.toList() }
        }
    }
    Last7DaysChart(modelProducer, modifier, data = data)
}