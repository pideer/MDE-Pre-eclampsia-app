package io.github.pideer.pes.ble.parsers

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toTz(): Int? =
    firstOrNull()?.toInt()

fun ByteArray.toUnixTime(): Long? =
    if (this.size != 8) return null
    else return ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .int.toLong() and 0xFFFFFFFF // Convert unsigned int to long

fun Long.toByteArray(): ByteArray =
    ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(this).array()