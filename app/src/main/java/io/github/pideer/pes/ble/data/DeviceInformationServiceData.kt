package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile

data class DeviceInformationServiceData(
    override val profile: Profile = Profile.DEVICE_INFORMATION,
    val modelNumber: String? = null,
    val serialNumber: String? = null,
    val firmwareRevision: String? = null,
    val hardwareRevision: String? = null,
    val softwareRevision: String? = null,
    val manufacturer: String? = null
): ProfileServiceData()
