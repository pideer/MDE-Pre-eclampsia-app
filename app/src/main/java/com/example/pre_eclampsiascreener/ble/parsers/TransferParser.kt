package com.example.pre_eclampsiascreener.ble.parsers

import android.util.Log
import com.example.pre_eclampsiascreener.ble.data.TransferServiceData
import com.example.pre_eclampsiascreener.data.Payload
import com.example.pre_eclampsiascreener.data.PayloadType
import com.example.pre_eclampsiascreener.data.SensorData
import com.example.pre_eclampsiascreener.data.TransferHeaderData
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val HEADER_SIZE = 13
private const val SENSOR_SIZE = 4

fun ByteArray.parseTransferData(): TransferServiceData? {
    val header: TransferHeaderData = this.copyOfRange(0, HEADER_SIZE).toHeader() ?: return null
    val payload: ByteArray = this.copyOfRange(HEADER_SIZE, size)

    return when (header.type) {
        PayloadType.SENSOR -> TransferServiceData(
            header = header, payload = Payload.Sensor(payload.toSensorData() ?: return null)
        )

        else -> TransferServiceData(
            header = header, payload = Payload.Debug(payload.toString(Charsets.UTF_8))
        )
    }
}

fun ByteArray.toHeader(): TransferHeaderData? {
    if (size != HEADER_SIZE) return null
    val buf = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    return TransferHeaderData(
        type = buf.get().toPayloadType(), length = buf.int.toUInt(), timestamp = buf.long
    )
}

fun Byte.toPayloadType(): PayloadType = when (this.toInt()) {
    0 -> PayloadType.SENSOR
    else -> PayloadType.DEBUG
}

fun ByteArray.toSensorData(): SensorData? {
    if (size < SENSOR_SIZE) return null
    return SensorData(
        this[0].toInt() and 0xFF,
        this[1].toInt() and 0xFF,
        this[2].toInt() and 0xFF,
        this[3].toUInt(),
        )
}

fun ByteArray.toInt(): Int? {
    if (size != Int.SIZE_BYTES) return null
    return ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).int
}

fun ByteArray.toUInt(): UInt? {
    if (size != UInt.SIZE_BYTES) return null
    return ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
}

fun ByteArray.toFloat(): Float? {
    Log.d("BleData", this.contentToString())
    if (size != Float.SIZE_BYTES) {
        Log.e("parser", "Bad size: $size")
        return null
    }
    val buf = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)

    return buf.getFloat()
}