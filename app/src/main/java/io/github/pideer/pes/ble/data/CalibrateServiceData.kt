package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile

data class CalibrateServiceData (
    override val profile: Profile = Profile.CALIBRATE,
    val ready: Boolean = false,
    val state: Int = 0,
): ProfileServiceData()