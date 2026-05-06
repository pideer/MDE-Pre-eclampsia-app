package io.github.pideer.pes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.ble.ConnectState
import io.github.pideer.pes.ui.theme.AppTheme

@Preview(showBackground = true)
@Composable
fun DevicesPreviewIdle() {
    AppTheme {
        LazyColumn(
            Modifier
            .fillMaxSize()
            .padding(0.dp, 3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            item {
                DeviceCard(
                    "PES-2446",
                    "FD:6D:43:3B:F0:E8",
                    -90,
                    ConnectState.Connecting,
                    modifier = Modifier.fillMaxWidth(),
                    {}
                )
            }
            item {
                DeviceCard(
                    "PES-3557",
                    "FD:6D:43:3B:F1:D9",
                    -90,
                    ConnectState.Idle,
                    modifier = Modifier.fillMaxWidth(),
                    {}
                )
            }
            item {
                DeviceCard(
                    "PES-6767",
                    "FD:6D:43:67:67:67",
                    -90,
                    ConnectState.Idle,
                    modifier = Modifier.fillMaxWidth(),
                    {}
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DevicePreviewIdle() {
    DeviceCard(null, "FD:6D:43:3B:F0:E8", -90, ConnectState.Idle,modifier = Modifier.fillMaxWidth(),{})
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewLoading() {
    DeviceCard(null, "FD:6D:43:3B:F0:E8", -90, ConnectState.Connecting,modifier = Modifier.fillMaxWidth(),{})
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewError() {
    DeviceCard(null, "FD:6D:43:3B:F0:E8", -90, ConnectState.Failed,modifier = Modifier.fillMaxWidth(),{})
}

@Composable
fun DeviceCard(
    name: String?,
    address: String,
    rssi: Int,
    connectState: ConnectState = ConnectState.Idle,
    modifier: Modifier = Modifier,
    onDeviceClick: () -> Unit,
) {
    val isAbleToConnect = connectState != ConnectState.Connecting
    val isFailed = connectState == ConnectState.Failed

    Card(
        onClick   = onDeviceClick,
        enabled   = isAbleToConnect,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor         = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor   = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border    = if (isFailed)
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        else null,
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Device icon badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFailed)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = if (isFailed) Icons.Default.Close else Icons.Outlined.Bluetooth,
                        contentDescription = null,
                        tint               = if (isFailed)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Name + address
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = name ?: "Unknown Device",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Debug only — remove when rssi is no longer needed
//                Text(
//                    text  = "rssi: $rssi",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.outline,
//                )
            }

            // Trailing state indicator
            when (connectState) {
                ConnectState.Connecting -> CircularProgressIndicator(
                    modifier    = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.primary,
                )

                ConnectState.Failed -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text  = "Failed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text  = "Tap to retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> Text(
                    text  = "Connect →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}