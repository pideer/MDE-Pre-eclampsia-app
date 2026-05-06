package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile

data class BatteryServiceData (
    override val profile: Profile = Profile.BATTERY,
    val batteryLevel: Int? = null,
): ProfileServiceData()