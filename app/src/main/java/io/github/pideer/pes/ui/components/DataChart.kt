package io.github.pideer.pes.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import java.util.Date

@Composable
fun DataChart(
    modelProducer: CartesianChartModelProducer,
    timestampList: List<Long>,
    systolicList: List<Int>,
    diastolicList: List<Int>,
    systolicBand: HorizontalBox,
    diastolicBand: HorizontalBox,
    rangeProvider: CartesianLayerRangeProvider,
    modifier: Modifier = Modifier
){
    // line formats
    val systolicLine = rememberSystolicLine()
    val diastolicLine = rememberDiastolicLine()
    val dateTimeFormatter = rememberDateTimeFormat()
    val bottomAxisFormatter = remember(timestampList) {
        CartesianValueFormatter { _, value, _ ->
            val index = value.toInt().coerceIn(0, timestampList.lastIndex)
            dateTimeFormatter.format(Date(timestampList[index]))
        }
    }

    val startAxisFormatter = remember {
        CartesianValueFormatter { _, value, _ -> value.toInt().toString() }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider  = LineCartesianLayer.LineProvider.series(
                    systolicLine, diastolicLine,
                ),
                rangeProvider = rangeProvider,
                pointSpacing = 1.dp
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = startAxisFormatter,
                itemPlacer     = VerticalAxis.ItemPlacer.step({ 10.0 }),
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter       = bottomAxisFormatter,
                labelRotationDegrees = 45f,
                itemPlacer           = HorizontalAxis.ItemPlacer.aligned(
                    spacing = { maxOf(1, timestampList.size / 10) }
                ),
            ),
            decorations = listOf(
                systolicBand,   // drawn first = behind
                diastolicBand,
            ),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        animateIn = false,
    )
}

@Composable
fun rememberDiastolicLine(): LineCartesianLayer.Line {
    return LineCartesianLayer.rememberLine(
        fill           = LineCartesianLayer.LineFill.single(Fill(colorResource(R.color.diastolic))),
        stroke         = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.dp),
        pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.4f),
        pointProvider  = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = rememberShapeComponent(Fill(colorResource(R.color.diastolic)), CircleShape),
                size      = 6.dp,
            )
        ),
    )
}

@Composable
fun rememberSystolicLine(): LineCartesianLayer.Line {
    return LineCartesianLayer.rememberLine(
        fill           = LineCartesianLayer.LineFill.single(Fill(colorResource(R.color.systolic))),
        stroke         = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.dp),
        pointConnector = LineCartesianLayer.PointConnector.cubic(curvature = 0.4f),
        pointProvider  = LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                component = rememberShapeComponent(Fill(colorResource(R.color.systolic)), CircleShape),
                size      = 6.dp,
            )
        ),
    )
}