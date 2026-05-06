package io.github.pideer.pes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.ui.theme.AppTheme

// Shared composable for both states
@Composable
fun BluetoothActionCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(32.dp),
    ) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(
            subtitle,
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick  = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
        ) {
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothActionCardPreview(){
    AppTheme() {
        BluetoothActionCard(
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            title = "Permissions required",
            subtitle = "Allow Bluetooth access to find and connect to your device",
            buttonText = "Grant permissions",
            onClick = { },
            Modifier.fillMaxSize()
        )
    }
}