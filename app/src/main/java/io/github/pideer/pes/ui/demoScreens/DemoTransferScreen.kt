package io.github.pideer.pes.ui.demoScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.ui.screens.BloodPressureChartPreview
import io.github.pideer.pes.ui.screens.ViewDataScreen

@Composable
fun DemoTransferScreen(
    modifier: Modifier = Modifier, onNext: () -> Unit, onResetClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {

        ViewDataScreen(
            modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        )
        Row() {
            ResetButton(onResetClick = onResetClick)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onNext, modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDemoTransfer() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {

            BloodPressureChartPreview(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            )  // ← add weight here

            Row {
                ResetButton(
                    onResetClick = {})
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {}, modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}