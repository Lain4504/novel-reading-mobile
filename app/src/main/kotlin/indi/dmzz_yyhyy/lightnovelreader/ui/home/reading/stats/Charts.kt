package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarTooltip
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.asSolidChartColor
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Last7DaysChart(
    modifier: Modifier = Modifier,
    target: Float?,
    data: List<BarData>
) {
    BarChart(
        modifier = modifier,
        target = target,
        barTooltip = BarTooltip.GraphTop,
        labelConfig = LabelConfig.default().copy(
            showXLabel = true,
            xAxisCharCount = 5,
            showYLabel = true,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer.asSolidChartColor()
        ),
        barChartColorConfig = BarChartColorConfig.default().copy(
            fillBarColor = MaterialTheme.colorScheme.onPrimaryContainer.asSolidChartColor()
        ),
        data = { data },
        barChartConfig = BarChartConfig.default().copy(
            cornerRadius = CornerRadius(40F, 40F),
        ),
        onBarClick = { index, barData -> println("click in bar with $index index and data $barData") })
}

@Composable
fun WeeklyCountChart(
    countList: Map<LocalDate, Count>
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")
    val data = countList.toList().takeLast(7).map { (date, count) ->
        DayData(
            dayLabel = date.format(dateFormatter),
            hourValues = List(24) { hour -> count.getMinute(hour) }
        )
    }

    CountChart(
        data = { data },
        modifier = Modifier
            .fillMaxSize()
            .height(320.dp)
            .padding(16.dp),
        config = CountChartConfig(
            barColor = MaterialTheme.colorScheme.onPrimaryContainer,
            daySpacing = 12.dp
        ),
        labelConfig = LabelConfig(
            showXLabel = true,
            showYLabel = true,
            textColor = Color.DarkGray.asSolidChartColor(),
            xAxisCharCount = 7,
            labelTextStyle = null,
        )
    )
}