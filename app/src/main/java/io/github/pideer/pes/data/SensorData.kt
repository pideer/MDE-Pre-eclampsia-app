package io.github.pideer.pes.data

data class SensorData(
    val heartRate: Int,
    val systolic: Int,
    val diastolic: Int,
    val code: UInt
)
