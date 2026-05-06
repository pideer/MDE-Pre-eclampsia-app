package io.github.pideer.pes.ble.repo

import io.github.pideer.pes.ble.data.DeviceInformationServiceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object DeviceInfoRepository {
    private val _data = MutableStateFlow(DeviceInformationServiceData())
    val data: StateFlow<DeviceInformationServiceData> = _data.asStateFlow()

    private val _deviceName = MutableStateFlow(String())
    val deviceName: StateFlow<String> = _deviceName.asStateFlow()

    fun setModelNum(modelNum: String?) =
            _data.update { it.copy(modelNumber = modelNum)}

    fun setSerialNum(serialNum: String?) =
        _data.update { it.copy(serialNumber = serialNum)}

    fun setFirmwareRev(firmwareRev: String?) =
        _data.update { it.copy(firmwareRevision = firmwareRev)}

    fun setHardwareRev(hardwareRev: String?) =
        _data.update { it.copy(hardwareRevision = hardwareRev)}

    fun setSoftwareRev(softwareRev: String?) =
        _data.update { it.copy(softwareRevision = softwareRev)}

    fun setManufacturer(manufacturer: String?) =
        _data.update { it.copy(manufacturer = manufacturer)}

    fun setDeviceName(name: String?) =
        _deviceName.update { name ?: "Unknown"}

    fun clear() =
        _data.update { DeviceInformationServiceData() }

}