package io.github.pideer.pes.ble.parsers

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val SIZE_OF_4_BYTES = 4 * Byte.SIZE_BYTES
private const val SIZE_OF_4_FLOATS = 4 * Float.SIZE_BYTES

fun ByteArray.toBoolean(): Boolean {
    Log.d("parser", "Boolean Input: ${this.contentToString()}")
    return isNotEmpty() && this[0] != 0.toByte()
}

fun ByteArray.toMinMaxList(): List<Int>{
    Log.d("BleData", this.contentToString())
    if (size != SIZE_OF_4_BYTES) {
        Log.e("parser", "Bad size: $size")
        return emptyList()
    }

    return listOf(
        this[0].toInt() and 0xFF,
        this[1].toInt() and 0xFF,
        this[2].toInt() and 0xFF,
        this[3].toInt() and 0xFF,
    )
}

fun ByteArray.toFloatList(): List<Float>{
    Log.d("BleData", this.contentToString())
    if (size != SIZE_OF_4_FLOATS) {
        Log.e("parser", "Bad size: $size")
        return emptyList()
    }
    val buf = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)

    return listOf(
        buf.getFloat(),
        buf.getFloat(),
        buf.getFloat(),
        buf.getFloat(),
    )
}

fun Int.toByteArray(): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

fun Float.toByteArray(): ByteArray =
    ByteBuffer.allocate(Float.SIZE_BYTES).putFloat(this).array()

