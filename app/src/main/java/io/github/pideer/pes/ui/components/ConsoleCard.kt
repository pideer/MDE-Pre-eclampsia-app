package io.github.pideer.pes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun ConsoleCard(
    time: Long,
    msg: String?,
) {
    val utc = Instant.fromEpochSeconds(time).toLocalDateTime(TimeZone.UTC)
    val (level, text) = remember(msg) { parseLogMessage(msg ?: "") }
    val dotColor = level.indicatorColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
//            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        // Fixed-size slot so text always starts at the same x regardless of level
        if (dotColor != null) {
            Surface(
                shape    = CircleShape,
                color    = dotColor,
                modifier = Modifier.size(8.dp),
                content  = {},
            )
        } else {
            Spacer(modifier = Modifier.size(8.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text  = "$utc: $text",
            color = dotColor ?: MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun parseLogMessage(raw: String): Pair<LogLevel, String> {
    if (raw.isEmpty()) return Pair(LogLevel.STATUS, raw)
    val first = raw[0]
    return if (first.isDigit()) {
        Pair(LogLevel.fromCode(first.digitToInt()), raw.drop(1))
    } else {
        Pair(LogLevel.STATUS, raw)
    }
}

private enum class LogLevel(val code: Int) {
    ERROR(0),
    WARN(1),
    STATUS(2);

    companion object {
        fun fromCode(code: Int): LogLevel =
            entries.firstOrNull { it.code == code } ?: STATUS
    }
}

private val LogLevel.indicatorColor: Color?
    get() = when (this) {
        LogLevel.ERROR   -> Color(0xFFE53935)  // red
        LogLevel.WARN    -> Color(0xFFFFB300)  // amber
        LogLevel.STATUS  -> null
    }

@Preview(showBackground = true)
@Composable
fun ConsoleCardPreview() {
    Column(Modifier.fillMaxSize()) {
        ConsoleCard(1771558313L, "hey")
        ConsoleCard(1771558513L, "0Sensor disconnected unexpectedly")
        ConsoleCard(1771558813L, "1Battery level low")
        ConsoleCard(1771559513L, "Internal tick 4ms")
        ConsoleCard(1771560313L, "2This and that happened")
        ConsoleCard(1771560413L, "closing")
    }
}

@Preview(showBackground = true)
@Composable
fun ConsoleCardErrorPreview() {
    ConsoleCard(1771558313L, "0Sensor disconnected unexpectedly")
}

@Preview(showBackground = true)
@Composable
fun ConsoleCardWarnPreview() {
    ConsoleCard(1771558313L, "1Battery level low")
}

@Preview(showBackground = true)
@Composable
fun ConsoleCardStatusPreview() {
    ConsoleCard(1771558313L, "2This and that happened")
}

@Preview(showBackground = true)
@Composable
fun ConsoleCardVerbosePreview() {
    ConsoleCard(1771558313L, "Internal tick 4ms")
}