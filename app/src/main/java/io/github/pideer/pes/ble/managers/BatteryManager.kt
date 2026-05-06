package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.ServiceManager.Companion.TAG_DATA
import io.github.pideer.pes.ble.repo.BatteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class BatteryManager : ServiceManager {
    companion object {
        const val TAG = "BatterManager"
        private val BATTERY_LEVEL_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("00002A19-0000-1000-8000-00805f9b34fb")
    }

    override val profile: Profile = Profile.BATTERY

    override suspend fun observeServiceInteractions(
//        peripheral: Peripheral,
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        val batteryChar = remoteService.characteristics
            .firstOrNull { it.uuid == BATTERY_LEVEL_CHARACTERISTIC_UUID }

        try {
            batteryChar?.read()[0]?.toInt()
                .let {
                    Log.d(TAG_DATA, "Battery Level: $it")
                    BatteryRepository.updateBatteryLevel(it ?: -1)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading battery level: ${e.message}")
        }

        try {
            batteryChar?.subscribe()
                ?.mapNotNull { it[0].toInt() }
                ?.onEach {
                    Log.d(TAG_DATA, "Battery Level: $it")
                    BatteryRepository.updateBatteryLevel(it)
                }
                ?.onCompletion { BatteryRepository.clear() }
                ?.catch { e ->
                    Log.e(TAG, e.toString())
                }
                ?.launchIn(scope)
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to battery level: ${e.message}")
        }
    }
}