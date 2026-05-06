package com.example.pre_eclampsiascreener.ble.data

import com.example.pre_eclampsiascreener.ble.Profile

data class CalibrateServiceData (
    override val profile: Profile = Profile.CALIBRATE,
    val ready: Boolean = false,
    val state: Int = 0,
): ProfileServiceData()