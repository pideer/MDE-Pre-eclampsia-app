package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile

data class TimeServiceData (
    override val profile: Profile = Profile.TIME,
    val unixTime: Long? = null,
    val timezone: Int? = null
): ProfileServiceData()