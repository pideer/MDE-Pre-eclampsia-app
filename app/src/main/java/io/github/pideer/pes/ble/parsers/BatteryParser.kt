package io.github.pideer.pes.ble.parsers

object BatteryLevelParser {
    fun parse(data: ByteArray): Int? =
        if (data.size == 1) data[0].toInt() and 0xFF else null
}

fun ByteArray.toBatteryLevel(): Int? =
    if (this.size == 1) this[0].toInt() and 0xFF else null