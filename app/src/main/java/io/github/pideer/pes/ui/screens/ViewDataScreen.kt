package io.github.pideer.pes.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.R
import io.github.pideer.pes.ble.repo.ConfigRepository
import io.github.pideer.pes.ble.repo.TransferRepository
import io.github.pideer.pes.data.Payload
import io.github.pideer.pes.ui.components.DataChart
import io.github.pideer.pes.ui.components.DataTable
import io.github.pideer.pes.ui.components.LegendDot
import io.github.pideer.pes.ui.components.rememberNormalBand
import io.github.pideer.pes.ui.components.rememberRange
import io.github.pideer.pes.ui.theme.AppTheme
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import kotlinx.coroutines.runBlocking

@Composable
fun ViewDataScreen(
    modifier: Modifier
) {
    val config by ConfigRepository.data.collectAsState()
    val entries by TransferRepository.sensor.collectAsState()
// prep list
    val timestampList = entries.map { it.header.timestamp }
    val systolicList = entries.map { (it.payload as Payload.Sensor).data.systolic }
    val diastolicList = entries.map { (it.payload as Payload.Sensor).data.diastolic }

// prep values
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(timestampList, diastolicList, systolicList) {
        if (timestampList.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries {
                series(systolicList)   // series 0 → systolic
                series(diastolicList)  // series 1 → diastolic
            }
        }
    }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {

        Text(
            text = "Blood Pressure",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            LegendDot(color = colorResource(R.color.systolic), label = "Systolic")
            LegendDot(color = colorResource(R.color.diastolic), label = "Diastolic")
        }

        if (timestampList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("No readings available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column() {
                val thresholds = ConfigRepository.data.collectAsState()
                DataChart(
                    modelProducer,
                    timestampList,
                    systolicList,
                    diastolicList,
                    rememberNormalBand(
                        thresholds.value.systolicMin?.toDouble() ?: 80.0,
                        thresholds.value.systolicMax?.toDouble() ?: 120.0,
                        colorResource(R.color.systolic_band)
                    ),
                    rememberNormalBand(
                        thresholds.value.diastolicMin?.toDouble() ?: 40.0,
                        thresholds.value.diastolicMax?.toDouble() ?: 80.0,
                        colorResource(R.color.diastolic_band)
                    ),
                    rememberRange(
                        config.diastolicMin?.toDouble(),
                        config.systolicMax?.toDouble()
                    ),
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp),  // extra height absorbs the 45° labels
                )
                DataTable(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    timestampList,
                    systolicList,
                    diastolicList,
                    thresholds.value.systolicMax ?: 120,
                    thresholds.value.diastolicMax ?: 80,
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BloodPressureChartPreview(modifier: Modifier = Modifier.fillMaxSize()) {
    val sampleTime = listOf(
        1744844400000L,   // 6 nights ago: 2026-04-10 23:00 EDT
        1744930800000L,   // 5 nights ago: 2026-04-11 23:00 EDT
        1745017200000L,   // 4 nights ago: 2026-04-12 23:00 EDT
        1745103600000L,   // 3 nights ago: 2026-04-13 23:00 EDT
        1745190000000L,   // 2 nights ago: 2026-04-14 23:00 EDT
        1745276400000L,   // 1 night ago : 2026-04-15 23:00 EDT
        1745362800000L,   // Tonight      : 2026-04-16 23:00 EDT
        1744844400000L,   // 6 nights ago: 2026-04-10 23:00 EDT
        1744930800000L,   // 5 nights ago: 2026-04-11 23:00 EDT
        1745017200000L,   // 4 nights ago: 2026-04-12 23:00 EDT
        1745103600000L,   // 3 nights ago: 2026-04-13 23:00 EDT
        1745190000000L,   // 2 nights ago: 2026-04-14 23:00 EDT
        1745276400000L,   // 1 night ago : 2026-04-15 23:00 EDT
        1745362800000L    // Tonight      : 2026-04-16 23:00 EDT
    )

    val sampleSystolic = listOf(
        125,
        118,
        110,
        118,
        122,
        128,
        145,
        125,
        118,
        110,
        118,
        122,
        128,
        145,
    )

    val sampleDiastolic = listOf(
        82,
        88,
        85,
        76,
        79,
        84,
        93,
        82,
        88,
        85,
        76,
        79,
        84,
        93,
    )

    val modelProducer = remember { CartesianChartModelProducer() }

    // runBlocking is only acceptable in previews — never in production code
    runBlocking {
        modelProducer.runTransaction {
            lineSeries {
                series(sampleSystolic)
                series(sampleDiastolic)
            }
        }
    }
    AppTheme {
        Column(
            modifier = modifier
                .padding(16.dp)
        ) {

            Text(
                text = "Blood Pressure",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                LegendDot(color = colorResource(R.color.systolic), label = "Systolic")
                LegendDot(color = colorResource(R.color.diastolic), label = "Diastolic")
            }

            DataChart(
                modelProducer,
                sampleTime,
                sampleSystolic,
                sampleDiastolic,
                rememberNormalBand(90.0, 129.0, colorResource(R.color.systolic_band)),
                rememberNormalBand(40.0, 90.0, colorResource(R.color.diastolic_band)),
                rememberRange(
                    40.0,
                    129.0,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            )
            DataTable(
                Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                sampleTime,
                sampleSystolic,
                sampleDiastolic,
                129,
                90
            )
        }
    }
}