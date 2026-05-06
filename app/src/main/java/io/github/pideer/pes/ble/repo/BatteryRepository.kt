package io.github.pideer.pes.ble.repo

import io.github.pideer.pes.ble.data.BatteryServiceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object BatteryRepository {
    private val _data = MutableStateFlow(BatteryServiceData())
    val data: StateFlow<BatteryServiceData> = _data.asStateFlow()

    fun updateBatteryLevel(data: Int) {
        _data.update { it.copy(batteryLevel = data)}
    }

    fun clear() {
        _data.update { BatteryServiceData() }
    }
}