package io.github.pideer.pes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pideer.pes.R
import io.github.pideer.pes.ble.data.DeviceInformationServiceData
import io.github.pideer.pes.ble.repo.DeviceInfoRepository
import io.github.pideer.pes.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopAppBarPreview() {
    AppTheme() {
        Scaffold(
            topBar = {
                Banner {}
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                DeviceInfoBanner(
                    "PES-XXXX",
                    "0000001",
                    75
                )
                Text(
                    text = "hi",
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Banner(onBackClick: () -> Unit) {
    var showAboutDialog by remember { mutableStateOf(false) }

    val deviceInfo by DeviceInfoRepository.data.collectAsState()

    if (showAboutDialog) {
        AboutDialog(
            deviceInfo = deviceInfo,
            onDismiss = { showAboutDialog = false }
        )
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back"
                )
            }
        },
        title = {
            Text(stringResource(R.string.app_name))
        },
        actions = {
            IconButton(onClick = { showAboutDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    )
}


@Composable
fun AboutDialog(
    deviceInfo: DeviceInformationServiceData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
    }

    val hasDeviceInfo = deviceInfo.firmwareRevision != null || deviceInfo.hardwareRevision != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // App Version
                appVersion?.let {
                    InfoRow(label = "App Version", value = it)
                }

                // Device info section — only shown when at least one field is non-null
                if (hasDeviceInfo) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    deviceInfo.firmwareRevision?.let {
                        InfoRow(label = "Firmware", value = it)
                    }
                    deviceInfo.hardwareRevision?.let {
                        InfoRow(label = "Hardware", value = it)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Made with love
                Text(
                    text = "❤\uFE0F Made with love by Virginia Tech ECE S26-09",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // GitHub links
                GitHubLink(label = "App Source Code", url =  stringResource(R.string.app_repo_link))
                GitHubLink(label = "Firmware Source Code", url = stringResource(R.string.firmware_repo_link))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun GitHubLink(label: String, url: String) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { uriHandler.openUri(url) },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    }
}

@Preview
@Composable
fun PreviewDeviceInfoBanner(){
    AppTheme {
        DeviceInfoBanner("PES-XXXX", "0000001", 43)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Screen – About Dialog with device info")
@Composable
fun PreviewScreenAboutWithDeviceInfo() {
    AppTheme {
        Scaffold(
            topBar = { Banner {} }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                DeviceInfoBanner("PES-XXXX", "0000001", 75)
            }
        }
        AboutDialog(
            deviceInfo = DeviceInformationServiceData(
                firmwareRevision = "1.4.2",
                hardwareRevision = "B"
            ),
            onDismiss = {}
        )
    }
}

@Preview(name = "About Dialog – With device info")
@Composable
fun PreviewAboutDialogWithDeviceInfo() {
    AppTheme {
        AboutDialog(
            deviceInfo = DeviceInformationServiceData(
                firmwareRevision = "1.4.2",
                hardwareRevision = "B"
            ),
            onDismiss = {}
        )
    }
}

@Preview(name = "About Dialog – No device info (null)")
@Composable
fun PreviewAboutDialogNoDeviceInfo() {
    AppTheme {
        AboutDialog(
            deviceInfo = DeviceInformationServiceData(),
            onDismiss = {}
        )
    }
}


@Composable
fun DeviceInfoBanner(devID: String, patientID: String, battery: Int?){
    val (batteryIcon, batteryDescription) = when (battery) {
        in 0..4 -> R.drawable.outline_battery_android_0_24 to R.string.battery_0_description
        in 5..20 -> R.drawable.outline_battery_android_frame_1_24 to R.string.battery_1_description
        in 21..36 -> R.drawable.outline_battery_android_frame_2_24 to R.string.battery_2_description
        in 37..52 -> R.drawable.outline_battery_android_frame_3_24 to R.string.battery_3_description
        in 53..68 -> R.drawable.outline_battery_android_frame_4_24 to R.string.battery_4_description
        in 69..84 -> R.drawable.outline_battery_android_frame_5_24 to R.string.battery_5_description
        in 85..95 -> R.drawable.outline_battery_android_frame_6_24 to R.string.battery_6_description
        in 96..100 -> R.drawable.outline_battery_android_full_24 to R.string.battery_full_description
        else -> R.drawable.outline_battery_android_alert_24 to R.string.battery_alert_description
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 13.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =  devID,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            style     = MaterialTheme.typography.labelMedium,
            color     = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = "PID: $patientID",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style     = MaterialTheme.typography.labelMedium,
            color     = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(batteryIcon),
                contentDescription = stringResource(batteryDescription),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            Spacer(modifier= Modifier.width(4.dp))
            Text(
                text = "$battery%",
                style     = MaterialTheme.typography.labelMedium,
                color     = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

    }
}