package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.ServiceManager.Companion.TAG_DATA
import io.github.pideer.pes.ble.repo.DeviceInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeviceInfoManager : ServiceManager{
    private val TAG = "DeviceInfoManager"
    override val profile: Profile = Profile.DEVICE_INFORMATION

    private val MODEL_NUMBER_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00002A24-0000-1000-8000-00805f9b34fb")
    private val SERIAL_NUMBER_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00002A25-0000-1000-8000-00805f9b34fb")
    private val FIRMWARE_REVISION_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00002A26-0000-1000-8000-00805f9b34fb")
    private val HARDWARE_REVISION_CHARACTERISTIC_UUIDD: Uuid = Uuid.parse("00002A27-0000-1000-8000-00805f9b34fb")
    private val SOFTWARE_REVISION_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00002A28-0000-1000-8000-00805f9b34fb")
    private val MANUFACTERER_NAME_CHARACTERISTIC_UUID: Uuid = Uuid.parse("00002A29-0000-1000-8000-00805f9b34fb")


    override suspend fun observeServiceInteractions(
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == MODEL_NUMBER_CHARACTERISTIC_UUID }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Model: $this")
                        DeviceInfoRepository.setModelNum(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }

            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == SERIAL_NUMBER_CHARACTERISTIC_UUID }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Serial: $this")
                        DeviceInfoRepository.setSerialNum(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }

            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == FIRMWARE_REVISION_CHARACTERISTIC_UUID }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Firmware: $this")
                        DeviceInfoRepository.setFirmwareRev(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }

            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == HARDWARE_REVISION_CHARACTERISTIC_UUIDD }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Hardware: $this")
                        DeviceInfoRepository.setHardwareRev(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }

            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == SOFTWARE_REVISION_CHARACTERISTIC_UUID }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Software: $this")
                        DeviceInfoRepository.setSoftwareRev(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }

            try {
                remoteService.characteristics
                    .firstOrNull { it.uuid == MANUFACTERER_NAME_CHARACTERISTIC_UUID }
                    ?.read()?.toString(Charsets.UTF_8)
                    .apply {
                        Log.d(TAG_DATA, "Manufacturer: $this")
                        DeviceInfoRepository.setManufacturer(this)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Read Error: ${e.toString()}")
            }
        }
    }
}

