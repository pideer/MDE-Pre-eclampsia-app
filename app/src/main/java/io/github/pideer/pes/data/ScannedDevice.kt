package io.github.pideer.pes.data

import no.nordicsemi.kotlin.ble.client.android.Peripheral

data class ScannedDevice(
    val peripheral: Peripheral,
    val rssi: Int
)