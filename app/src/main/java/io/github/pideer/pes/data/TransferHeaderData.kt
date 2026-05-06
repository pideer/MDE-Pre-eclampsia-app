package io.github.pideer.pes.data

data class TransferHeaderData(
    val type: PayloadType,
    val length: UInt,
    val timestamp: Long
)
