package io.github.pideer.pes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import java.util.Date

@Composable
fun DataTable(
    modifier: Modifier = Modifier,
    timestampList: List<Long>,
    systolicList: List<Int>,
    diastolicList: List<Int>,
    systolicHigh: Int, diastolicHigh:Int
) {
    LazyColumn(
        modifier = modifier
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.06.em
                )
                Text(
                    "BLOOD PRESSURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.06.em
                )
            }
            HorizontalDivider(thickness = 0.5.dp)
        }

        items(timestampList.size) { i ->
            val date = Date(timestampList[i])
            val formatted = rememberDateTimeFormat().format(date)
            val sys = systolicList[i]
            val dia = diastolicList[i]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hoverable(interactionSource = remember { MutableInteractionSource() })
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BpBadge(sys, dia, systolicHigh, diastolicHigh)
                    Text(
                        "$sys/$dia",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (i < timestampList.lastIndex) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

    @Composable
    fun BpBadge(sys: Int, dia: Int, systolicHigh: Int, diastolicHigh:Int) {
        val (label, containerColor, contentColor) = when {
            sys >= systolicHigh || dia >= diastolicHigh -> Triple("Elevated", Color(0xFFFAEEDA), Color(0xFF854F0B))
            else -> Triple("Normal", Color(0xFFEAF3DE), Color(0xFF3B6D11))
        }
        Surface(
            shape = CircleShape,
            color = containerColor
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
