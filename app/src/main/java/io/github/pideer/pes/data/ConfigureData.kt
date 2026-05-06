package io.github.pideer.pes.data

data class ConfigureData(
    val patientID: String? = null,
    val minDiastolic: Int? = null,
    val minSystolic: Int? = null,
    val maxDiastolic: Int? = null,
    val maxSystolic: Int? = null,
    val timezone: Int = 0
)