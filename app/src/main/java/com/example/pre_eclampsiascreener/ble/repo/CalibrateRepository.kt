package com.example.pre_eclampsiascreener.ble.repo

import android.util.Log
import com.example.pre_eclampsiascreener.ble.data.CalibrateServiceData
import com.example.pre_eclampsiascreener.ble.data.TransferServiceData
import com.example.pre_eclampsiascreener.ble.managers.CalibrateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CalibrateRepository {
    private val _sensor = MutableStateFlow<List<Float>>(emptyList())
    val sensor: StateFlow<List<Float>> = _sensor.asStateFlow()

    private val _data = MutableStateFlow(CalibrateServiceData())
    val data: StateFlow<CalibrateServiceData> = _data.asStateFlow()

    fun updateReady(value: Boolean) =
        _data.update { it.copy(ready = value) }

    fun incrementDemoState() {
        Log.d("Demo", "Incrementing state")
        _data.update { it.copy(state = it.state + 1) }
    }

    suspend fun triggerDemo() {
        CalibrateManager.writeTrigger()
        _data.update { it.copy(state = 0) }
        TransferRepository.clear()
    }
    fun addStreamItem(value: Boolean) =
        _data.update { it.copy(ready = value) }

    fun addStreamItem(value: Float) {
        _sensor.value += value
    }

    fun clear() =
        _data.update { CalibrateServiceData() }

    fun addSample(value: Float) {
        _sensor.value += value
    }
}