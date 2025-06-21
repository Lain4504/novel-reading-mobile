package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.LabelConfig

/**
 * 代表单日24小时的数据
 * @param dayLabel 日期标签（如"Mon"）
 * @param hourValues 24个数值（0-60），分别对应0-23时
 */
data class DayData(
    val dayLabel: String,
    val hourValues: List<Int>
) {
    init {
        require(hourValues.size == 24) { "必须提供24小时的数值" }
    }
}

/**
 * 计数图配置参数
 * @param barColor 柱状图基础颜色
 * @param axisColor 轴线颜色
 * @param gridColor 网格线颜色
 * @param showAxis 是否显示轴线
 * @param showGrid 是否显示网格
 * @param daySpacing 日期之间的间距
 */
data class CountChartConfig(
    val barColor: Color = Color.Blue,
    val axisColor: Color = Color.Black,
    val gridColor: Color = Color.Gray.copy(alpha = 0.3f),
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val daySpacing: Dp = 8.dp
) {
    companion object {
        fun default() = CountChartConfig()
    }
}

@Composable
fun CountChart(
    data: () -> List<DayData>,
    modifier: Modifier = Modifier,
    config: CountChartConfig = CountChartConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default()
) {
    val daysData = data()
    require(daysData.size == 7) { "data size must be 7" }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { if (config.showGrid) drawGrid(config) }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val daySpacingPx = config.daySpacing.toPx()

        val barWidth = (canvasWidth - daySpacingPx * 6) / 7
        val hourHeight = canvasHeight / 24f

        daysData.forEachIndexed { dayIndex, dayData ->
            val startX = dayIndex * (barWidth + daySpacingPx)

            dayData.hourValues.forEachIndexed { hourIndex, value ->
                val alpha = (value / 60f).coerceIn(0f, 1f)
                val yPos = canvasHeight - (hourIndex + 1) * hourHeight

                drawRect(
                    color = config.barColor.copy(alpha = alpha),
                    topLeft = Offset(startX, yPos),
                    size = Size(barWidth, hourHeight)
                )
            }
        }

        drawLabelsAndAxis(daysData, labelConfig, textMeasurer, barWidth, daySpacingPx, canvasHeight)
        if (config.showAxis) drawAxis(config)
    }
}

private fun DrawScope.drawGrid(config: CountChartConfig) {
    val gridInterval = size.height / 6
    for (i in 1..5) {
        drawLine(
            color = config.gridColor,
            start = Offset(0f, i * gridInterval),
            end = Offset(size.width, i * gridInterval),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawAxis(config: CountChartConfig) {
    drawLine(
        color = config.axisColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawLabelsAndAxis(
    daysData: List<DayData>,
    labelConfig: LabelConfig,
    textMeasurer: TextMeasurer,
    barWidth: Float,
    daySpacing: Float,
    canvasHeight: Float
) {
    if (labelConfig.showXLabel) {
        daysData.forEachIndexed { index, dayData ->
            val text = dayData.dayLabel
            val textLayout = textMeasurer.measure(
                text = text,
            )

            val xPos = index * (barWidth + daySpacing) + barWidth / 2
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = xPos - textLayout.size.width / 2,
                    y = size.height + 4.dp.toPx()
                ),
                color = labelConfig.textColor.value.first()
            )
        }
    }

    if (labelConfig.showYLabel) {
        for (hour in 0..23 step 3) {
            val text = "$hour"
            val textLayout = textMeasurer.measure(text)

            val yPos = canvasHeight - (hour + 1) * (size.height / 24)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = -textLayout.size.width - 8.dp.toPx(),
                    y = yPos - textLayout.size.height / 2
                ),
                color = labelConfig.textColor.value.first()
            )
        }
    }
}