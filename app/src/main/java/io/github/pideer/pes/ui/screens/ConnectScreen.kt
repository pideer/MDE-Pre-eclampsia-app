package io.github.pideer.pes.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.pideer.pes.ble.ConnectState
import io.github.pideer.pes.ui.components.BluetoothActionCard
import io.github.pideer.pes.ui.components.DeviceCard
import io.github.pideer.pes.ui.components.EmptyState
import io.github.pideer.pes.ui.state.NavEvent
import io.github.pideer.pes.ui.viewmodels.ScanViewModel
import kotlinx.coroutines.delay
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.android.AndroidEnvironment
import no.nordicsemi.kotlin.ble.environment.android.compose.LocalEnvironmentOwner.current

@Composable
fun ConnectScreen(
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ScanViewModel = viewModel(),
) {
    val environment = current
    val state by vm.uiState.collectAsState()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            AndroidEnvironment.Permission.BLUETOOTH_SCAN,
            AndroidEnvironment.Permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }
    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                environment.isBluetoothScanPermissionGranted &&
                        environment.isBluetoothConnectPermissionGranted
            } else {
                environment.isLocationPermissionGranted
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permissionGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    environment.isBluetoothScanPermissionGranted &&
                            environment.isBluetoothConnectPermissionGranted
                } else {
                    environment.isLocationPermissionGranted
                }
        }
    )

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    LifecycleResumeEffect(Unit) {
        vm.disconnect()
        if (permissionGranted && state.bluetoothState == Manager.State.POWERED_ON) {
            vm.startScan()
        }
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        vm.navigationEvent.collect { event ->
            if(event == NavEvent.GoToMenu)
                onSuccess()
        }
    }

    if (state.connectState == ConnectState.Failed) {
        LaunchedEffect(Unit) {
            delay(5000)                    // Show failure briefly
            vm.resetConnectionState()      // Go back to Idle state
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopScan()
        }
    }

    val devices by vm.peripherals.collectAsStateWithLifecycle()
    Box(modifier) {
        when {
            state.bluetoothState != Manager.State.POWERED_ON -> {
                BluetoothActionCard(
                    icon = {
                        Icon(
                            Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    },
                    title = "Bluetooth is off",
                    subtitle = "Turn on Bluetooth to scan for nearby devices",
                    buttonText = "Enable Bluetooth",
                    onClick = {
                        enableBluetoothLauncher.launch(
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        )
                    }
                )
            }

            !permissionGranted -> {
                BluetoothActionCard(
                    icon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    title = "Permissions required",
                    subtitle = "Allow Bluetooth access to find and connect to your device",
                    buttonText = "Grant permissions",
                    onClick = { permissionLauncher.launch(permissions) },
                )
            }

            devices.isEmpty() -> EmptyState("No devices found. Make sure the device is charging.")

            else -> {
                LazyColumn(
                    modifier = modifier.padding(0.dp, 3.dp)
                ) {
                    items(devices) { device ->
                        val isThisDeviceSelected = state.selectedPeripheral == device
                        val cs =
                            if (isThisDeviceSelected) state.connectState else ConnectState.Idle
                        DeviceCard(
                            device.name, device.address, -1, cs,
                            Modifier.fillMaxWidth(),
                        ) {
                            vm.onPeripheralSelected(device)
                        }
//                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
