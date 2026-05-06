package io.github.pideer.pes.ble.data

data class ConfigData(
    val demoMode: Boolean = false,
    val pid: UInt? = null,
    val diastolicMin: Int?= null,
    val diastolicMax: Int?= null,
    val diastolicCoeff_m: Float?= null,
    val diastolicCoeff_b: Float?= null,
    val systolicMin: Int?= null,
    val systolicMax: Int?= null,
    val systolicCoeff_m: Float?= null,
    val systolicCoeff_b: Float?= null,
)
