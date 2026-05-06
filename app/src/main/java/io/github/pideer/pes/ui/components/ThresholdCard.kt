package io.github.pideer.pes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ThresholdCard(
    currentMinSystolic: Int?,
    currentMinDiastolic: Int?,
    currentMaxSystolic: Int?,
    currentMaxDiastolic: Int?,
    minSystolic: Int?,
    minDiastolic: Int?,
    maxSystolic: Int?,
    maxDiastolic: Int?,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit
) {
    // Derive display strings internally
    val minBloodValue = remember(minSystolic, minDiastolic) {
        if (minSystolic == null && minDiastolic == null) ""
        else "${minSystolic ?: ""}/${minDiastolic ?: ""}"
    }
    val maxBloodValue = remember(maxSystolic, maxDiastolic) {
        if (maxSystolic == null && maxDiastolic == null) ""
        else "${maxSystolic ?: ""}/${maxDiastolic ?: ""}"
    }

    val currentMinValue = remember(currentMinSystolic, currentMinDiastolic) {
        if (currentMinSystolic == null && currentMinDiastolic == null) null
        else "${currentMinSystolic ?: ""}/${currentMinDiastolic ?: ""}"
    }
    val currentMaxValue = remember(currentMaxSystolic, currentMaxDiastolic) {
        if (currentMaxSystolic == null && currentMaxDiastolic == null) null
        else "${currentMaxSystolic ?: ""}/${currentMaxDiastolic ?: ""}"
    }

    // Validation: must be empty OR match digits/slash and have valid ranges
    fun isValidBp(value: String): Boolean {
        if (value.isEmpty()) return true
        val regex = Regex("""^\d{1,3}/\d{1,3}$""")
        if (!regex.matches(value)) return false
        val parts = value.split("/")
        val sys = parts[0].toIntOrNull() ?: return false
        val dia = parts[1].toIntOrNull() ?: return false
        return sys in 40..300 && dia in 20..200
    }

    // Only allow digits and a single slash while typing
    fun sanitize(input: String): String =
        input.filter { it.isDigit() || it == '/' }
            .let { s ->
                // Allow at most one slash
                val slashIdx = s.indexOf('/')
                if (slashIdx == -1) s
                else s.substring(0, slashIdx + 1) + s.substring(slashIdx + 1).replace("/", "")
            }

    val minIsError = minBloodValue.isNotEmpty() && !isValidBp(minBloodValue)
    val maxIsError = maxBloodValue.isNotEmpty() && !isValidBp(maxBloodValue)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Blood Pressure Thresholds",
                style = MaterialTheme.typography.titleMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = minBloodValue,
                    onValueChange = { onMinChange(sanitize(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Minimum") },
                    placeholder = { Text(currentMinValue ?: "90/60") },
                    singleLine = true,
                    isError = minIsError,
                    supportingText = when {
                        minIsError -> {
                            { Text("Use format SYS/DIA\ne.g. 90/60") }
                        }

                        currentMinValue != null -> {
                            { Text("Current: $currentMinValue") }
                        }

                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = maxBloodValue,
                    onValueChange = { onMaxChange(sanitize(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Maximum") },
                    placeholder = { Text(currentMaxValue ?: "120/80") },
                    singleLine = true,
                    isError = maxIsError,
                    supportingText = when {
                        maxIsError -> {
                            { Text("Use format SYS/DIA\ne.g. 120/80") }
                        }

                        currentMaxValue != null -> {
                            { Text("Current: $currentMaxValue") }
                        }

                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }
}

private fun parseBloodPressure(input: String): Pair<Int?, Int?> {
    val parts = input.split("/")
    val systolic = parts.getOrNull(0)?.trim()?.toIntOrNull()
        ?.takeIf { it in 40..300 }
    val diastolic = parts.getOrNull(1)?.trim()?.toIntOrNull()
        ?.takeIf { it in 20..200 }
    return systolic to diastolic
}