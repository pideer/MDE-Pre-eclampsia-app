package io.github.pideer.pes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun rememberDateTimeFormat(): SimpleDateFormat {
    return remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            shape    = CircleShape,
            color    = color,
            modifier = Modifier.size(10.dp),
            content  = {},
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun rememberNormalBand(
    yMin: Double,
    yMax: Double,
    color: Color,
): HorizontalBox {
    val box = rememberShapeComponent(
        fill  = Fill(color),
        shape = RoundedCornerShape(0.dp),
    )
    return remember(yMin, yMax, color) {
        HorizontalBox(
            y              = { yMin..yMax },
            box            = box,
            labelComponent = null,
        )
    }
}

@Composable
fun rememberRange(
    minimumY: Double?,
    maximumY: Double?
): CartesianLayerRangeProvider {
    val miy = minimumY ?: 60.0
    val may = maximumY ?: 140.0
    return remember(miy, may) {
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                (minOf(minY, miy) - 10).coerceAtLeast(0.0)
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                maxOf(maxY, may) + 15
        }
    }
}