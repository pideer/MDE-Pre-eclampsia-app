package io.github.pideer.pes.ble.data

import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.data.TransferHeaderData
import io.github.pideer.pes.data.Payload

data class TransferServiceData(
    override val profile: Profile = Profile.TRANSFER,
    val header: TransferHeaderData,
    val payload: Payload
): ProfileServiceData()
