package io.github.pideer.pes.ble.repo

import io.github.pideer.pes.ble.data.ConfigData
import io.github.pideer.pes.ble.managers.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ConfigRepository {
    private val _data = MutableStateFlow(ConfigData())
    val data: StateFlow<ConfigData> = _data.asStateFlow()

    fun updateDemoMode(flag: Boolean) =
        _data.update { it.copy(demoMode = flag) }

    fun updatePID(pid: UInt?) =
        _data.update { it.copy(pid = pid) }

    fun updateThresholds(list: List<Int>) =
        _data.update {
            it.copy(
                systolicMin = list[0],
                systolicMax = list[1],
                diastolicMin = list[2],
                diastolicMax = list[3]
            )
        }

    fun updateCoefficients(list: List<Float>) =
        _data.update {
            it.copy(
                systolicCoeff_m = list[0],
                systolicCoeff_b = list[1],
                diastolicCoeff_m = list[2],
                diastolicCoeff_b = list[3]
            )
        }

//    fun updateSystolic(list: List<Int>) =
//        _data.update{it.copy(systolicMin = list[0], systolicMax = list[1])}
//
//    fun updateSystolicCoefficients(list: List<Float>) =
//        _data.update{it.copy(systolicCoeff_m = list[0], systolicCoeff_b = list[1])}

//    suspend fun startNewPatient()=
//        ConfigManager.writeNewPatient()

    suspend fun writePID(pid: Int) =
        ConfigManager.writePID(pid)

    suspend fun writeThresholds(
        systolic_min: Int,
        systolic_max: Int,
        diastolic_min: Int,
        diastolic_max: Int
    ) =
        ConfigManager.writeThreshold(
            listOf(
                systolic_min,
                systolic_max,
                diastolic_min,
                diastolic_max,
            )
        )

    suspend fun writeCoefficients(
        systolic_m: Float,
        systolic_b: Float,
        diastolic_m: Float,
        diastolic_b: Float
    ) =
        ConfigManager.writeCoefficients(
            listOf(
                systolic_m,
                systolic_b,
                diastolic_m,
                diastolic_b
            )
        )

//    suspend fun writeSystolic(min: Int, max: Int) =
//        ConfigManager.writeSystolic(listOf(min, max))
//
//    suspend fun writeSystolicCoefficients(m: Float, b: Float) =
//        ConfigManager.writeSystolicCoefficients(listOf(m, b))

    fun clear() {
        _data.update { ConfigData() }
    }
}