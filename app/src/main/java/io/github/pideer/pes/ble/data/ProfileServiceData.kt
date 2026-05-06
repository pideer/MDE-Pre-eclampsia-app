package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile

sealed class ProfileServiceData {
    abstract val profile: Profile
}