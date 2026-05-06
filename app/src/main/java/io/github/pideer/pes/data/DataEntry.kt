package io.github.pideer.pes.data

data class DataEntry(
    val time: Long, // unix time
    val pi: Int, // pulsatility index
    val diastolic: Int,
    val systolic: Int
)